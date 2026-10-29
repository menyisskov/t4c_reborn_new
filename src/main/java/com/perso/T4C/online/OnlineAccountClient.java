package com.perso.T4C.online;

import com.google.gson.Gson;
import com.perso.T4C.online.model.OnlineCharacterCreation;
import com.perso.T4C.online.model.OnlineProtocol;
import com.perso.T4C.online.model.OnlineRoster;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/** Account discovery/creation over one persistent game socket; call on a worker, never the render thread. */
public final class OnlineAccountClient implements AutoCloseable {
  private static final Gson JSON=new Gson();
  private record Hello(int protocolVersion,boolean persistent) {}
  private final URI endpoint;
  private final String accountKey;
  private final int configuredSocketPort;
  private GameSocketClient socket;
  private volatile boolean closed;

  public static final class Rejected extends IOException {
    private final int statusCode;
    private final String code;
    Rejected(int statusCode,String code) {
      super("Account request rejected (status "+statusCode+")");this.statusCode=statusCode;this.code=code;
    }
    public int statusCode() { return statusCode; }
    /** The server's short refusal code (for example {@code name_taken}), or null when absent or not a plain code. */
    public String code() { return code; }
  }

  public OnlineAccountClient(URI endpoint,String accountKey) {
    this(endpoint,accountKey,0);
  }

  OnlineAccountClient(URI endpoint,String accountKey,int socketPort) {
    this.endpoint=OnlineEndpoint.validate(endpoint);
    if(accountKey==null || !accountKey.matches("[0-9a-f]{64}"))
      throw new IllegalArgumentException("Invalid account key");
    if(socketPort<0 || socketPort>65535) throw new IllegalArgumentException("Invalid game socket port");
    this.accountKey=accountKey;configuredSocketPort=socketPort;
  }

  /** Explicit bounded page; the caller chooses whether to request the next page. */
  public synchronized OnlineRoster page(UUID after) throws IOException,InterruptedException {
    if(closed) throw new IOException("Account client is closed");
    requirePersistentMap();
    var roster=parse(request("GET","account/characters",after==null ? "" : "after="+after,
        null,"Bearer "+accountKey),OnlineRoster.class);
    validatePage(roster,after);
    return roster;
  }

  /** One explicit mutation; the caller retains its requestId for any uncertain-result retry. */
  public synchronized OnlineCharacterCreation.Result create(OnlineCharacterCreation.Request request)
      throws IOException,InterruptedException {
    Objects.requireNonNull(request);
    if(closed) throw new IOException("Account client is closed");
    requirePersistentMap(); // Never disclose the key to an incompatible or preview-only endpoint.
    String body=JSON.toJson(request);
    if(body.getBytes(StandardCharsets.UTF_8).length>512) throw new IllegalArgumentException("Creation request is too large");
    var received=parse(request("POST","account/characters","",body,"Bearer "+accountKey),
        OnlineCharacterCreation.Result.class);
    if(received==null || !request.requestId().equals(received.requestId()) || received.character()==null)
      throw new IOException("Invalid account server response");
    try {
      var c=received.character();
      var character=new OnlineRoster.CharacterSummary(c.id(),c.name(),c.characterClass(),c.gender(),
          c.level(),c.world(),c.availability());
      if(!request.characterClass().equals(character.characterClass()) || !request.gender().equals(character.gender()))
        throw new IllegalArgumentException("Unexpected character identity");
      return new OnlineCharacterCreation.Result(received.requestId(),character);
    } catch(RuntimeException invalid) { throw new IOException("Invalid account server response"); }
  }

  private void requirePersistentMap() throws IOException,InterruptedException {
    // Recheck before each account operation. The anonymous gate never carries the key.
    var hello=parse(request("GET","map","",null,null),Hello.class);
    if(hello==null || hello.protocolVersion()!=OnlineProtocol.VERSION || !hello.persistent())
      throw new IOException("Incompatible account server; update client and server together");
  }

  private static void validatePage(OnlineRoster roster,UUID after) throws IOException {
    if(roster==null || roster.characters()==null || roster.characters().size()>OnlineRoster.PAGE_SIZE)
      throw new IOException("Invalid account roster");
    String previous=after==null ? "" : after.toString();
    for(var character:roster.characters()) {
      if(character==null || character.id()==null || character.id().toString().compareTo(previous)<=0)
        throw new IOException("Invalid account roster order");
      previous=character.id().toString();
    }
    if(roster.nextAfter()!=null && (roster.characters().size()!=OnlineRoster.PAGE_SIZE
        || !roster.nextAfter().equals(roster.characters().getLast().id())))
      throw new IOException("Invalid account roster cursor");
  }

  private <T> T parse(String body,Class<T> type) throws IOException {
    try { return JSON.fromJson(body,type); }
    catch(RuntimeException invalid) { throw new IOException("Invalid account server response"); }
  }

  private String request(String method,String command,String query,String body,String authorization) throws IOException {
    if(closed) throw new IOException("Account client is closed");
    GameSocketProtocolReply received;
    try {
      if(socket==null) socket=GameSocketClient.connect(endpoint,
          configuredSocketPort==0 ? GameSocketClient.defaultPort(endpoint) : configuredSocketPort);
      var reply=socket.request(method,command,query,authorization,body);
      received=new GameSocketProtocolReply(reply.status(),reply.body());
    } catch(IOException | RuntimeException failed) {
      closeSocket();
      throw new IOException("Account request unavailable");
    }
    if(received.status()!=200) throw new Rejected(received.status(),refusalCode(received.body().getBytes(StandardCharsets.UTF_8)));
    return received.body();
  }

  private record GameSocketProtocolReply(int status,String body) {}

  private void closeSocket() {
    if(socket!=null) try { socket.close(); } catch(IOException ignored) { }
    socket=null;
  }

  private static final java.util.regex.Pattern REFUSAL=java.util.regex.Pattern.compile("\\{\"error\":\"([a-z_]{1,32})\"}");
  /** Only a strict {"error":"snake_case"} body is surfaced; anything else (including echoed secrets) is dropped. */
  private static String refusalCode(byte[] body) {
    if(body==null || body.length>256) return null;
    var matcher=REFUSAL.matcher(new String(body,StandardCharsets.UTF_8).strip());
    return matcher.matches() ? matcher.group(1) : null;
  }

  @Override public synchronized void close() { closed=true;closeSocket(); }
}
