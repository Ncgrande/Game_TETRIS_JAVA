Place your audio files here (or use the names below):

- bgm.mp3           : background music (looped)
- line.wav          : short sound effect played when lines are cleared
- levelup.wav       : short sound effect played when the player levels up

Recommended: small/optimized files (mp3 for music, wav for short SFX). The code checks for these files and will skip playback if they are missing.

How to test quickly:
- Add files with those exact names into this folder, then run:
  mvn -DskipTests javafx:run

If you prefer to bundle different file names, update the paths in `GameController` where resources are loaded.
