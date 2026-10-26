# Mythic creature concepts

T4C-0154 approved creature art, installed in the game sprite database.

- Obsidian Dragon: four-legged dark dragon with crimson wing membranes.
- Bronze Griffin: lion and eagle creature with gold-brown feathers.
- Swamp Hydra: three-headed green reptile.

Each creature has eight-frame front and rear walk and attack strips, plus an eight-frame death strip in `assets/sprites/source/mythic`. The front animation is shared by the 000, 045 and 090 directions; the rear animation is shared by 135 and 180. The engine mirrors these frames for the opposite directions. `MythicCreatureSpriteInstaller` packs the strips into the final sprite shard. `spritecook-assets.json` records the source asset IDs and checksums.

The Obsidian Dragon, Bronze Griffin and Swamp Hydra are level 1250, 1325 and 1400 bosses in separate clearings of Shattered Isle, reached after Outrider Kaelis's short passage quest. Their source still images remain in this folder for art review.
