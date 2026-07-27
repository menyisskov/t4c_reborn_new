package com.perso.T4C.editor.ui;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class EditorControl extends EditorComponent {
    protected boolean enabled = true;
    protected boolean hovered;
}
