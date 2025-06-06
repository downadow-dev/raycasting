/*
   Copyright 2025 downadow

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package downadow.raycasting;

import com.badlogic.gdx.files.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.Input.*;

public class Main implements ApplicationListener {
    ShapeRenderer shape;
    FitViewport viewport;
    Vector2 touch;
    final int WIDTH = 400, HEIGHT = 200;
    final float FOV  = 1.26f; // ~72
    final float VFOV = 0.7f;  // ~40
    final float MAXDIST = 9.5f;
    char map[][][] = new char[21][64][64];
    boolean forward = false, backward = false, left = false, right = false;
    boolean camUp = false, camDown = false, camLeft = false, camRight = false;
    boolean jump = false, save = false;
    int startX = 0, startY = 0;
    float playerX = 1.0f, playerY = 1.0f, playerZ = 1.7f, playerAngle = 0.79f, playerAngleZ = 0.001f;
    
    private void loadNewMap() {
        for(int z = 0; z < map.length; z++)
            for(int y = 0; y < map[z].length; y++)
                for(int x = 0; x < map[z][y].length; x++)
                    map[z][y][x] = (z == 0 ? '#' : '.');
    }
    
    public void create() {
        touch = new Vector2();
        shape = new ShapeRenderer();
        viewport = new FitViewport(WIDTH, HEIGHT);
        
        if(Gdx.files.local("map").exists()) {
            try {
                String content[] = Gdx.files.local("map").readString().split("\n");
                int line = 0;
                for(int z = 0; z < map.length; z++) {
                    for(int y = 0; y < map[z].length; y++, line++) {
                        char lineChars[] = content[line].toCharArray();
                        for(int x = 0; x < map[z][y].length; x++)
                            map[z][y][x] = lineChars[x];
                    }
                }
            } catch(Exception ex) { loadNewMap(); }
        } else loadNewMap();
        
        Gdx.input.setInputProcessor(new InputAdapter() {
            public boolean keyDown(int key) {
                if(key == Input.Keys.W) {
                    forward = true;
                    return true;
                } else if(key == Input.Keys.S) {
                    backward = true;
                    return true;
                } else if(key == Input.Keys.A) {
                    left = true;
                    return true;
                } else if(key == Input.Keys.D) {
                    right = true;
                    return true;
                } else if(key == Input.Keys.UP) {
                    camUp = true;
                    return true;
                } else if(key == Input.Keys.DOWN) {
                    camDown = true;
                    return true;
                } else if(key == Input.Keys.LEFT) {
                    camLeft = true;
                    return true;
                } else if(key == Input.Keys.RIGHT) {
                    camRight = true;
                    return true;
                } else if(key == Input.Keys.SPACE && !jump && map[(int)playerZ - 1][(int)playerY][(int)playerX] != '.') {
                    jump = true;
                    new Thread() {
                        public void run() {
                            try {
                                for(int i = 1; i <= 11; i++) {
                                    playerZ += 0.1f;
                                    Thread.sleep(i * 10);
                                }
                                Thread.sleep(140);
                            } catch(Exception ex) {}
                            jump = false;
                        }
                    }.start();
                } else if(key == Input.Keys.E) {
                    final float eX = (float)Math.cos(playerAngle);
                    final float eY = (float)Math.sin(playerAngle);
                    final float eZ = (float)Math.sin(playerAngleZ);
                        
                    float dist = 0.0f;
                    while(dist < MAXDIST && map[(int)(playerZ + eZ * dist)][(int)(playerY + eY * dist)][(int)(playerX + eX * dist)] == '.') {
                        dist += 0.05f;
                        if((int)(playerZ + eZ * dist) < 0 || (int)(playerZ + eZ * dist) >= map.length ||
                           (int)(playerY + eY * dist) < 0 || (int)(playerY + eY * dist) >= map[0].length ||
                           (int)(playerX + eX * dist) < 0 || (int)(playerX + eX * dist) >= map[0][0].length) return false;
                    }
                    
                    dist -= 0.05f;
                    map[(int)(playerZ + eZ * dist)][(int)(playerY + eY * dist)][(int)(playerX + eX * dist)] = '#';
                    return true;
                } else if(key == Input.Keys.Q) {
                    final float eX = (float)Math.cos(playerAngle);
                    final float eY = (float)Math.sin(playerAngle);
                    final float eZ = (float)Math.sin(playerAngleZ);
                        
                    float dist = 0.0f;
                    while(map[(int)(playerZ + eZ * dist)][(int)(playerY + eY * dist)][(int)(playerX + eX * dist)] == '.') {
                        dist += 0.05f;
                        if((int)(playerZ + eZ * dist) < 1 || (int)(playerZ + eZ * dist) >= map.length ||
                           (int)(playerY + eY * dist) < 0 || (int)(playerY + eY * dist) >= map[0].length ||
                           (int)(playerX + eX * dist) < 0 || (int)(playerX + eX * dist) >= map[0][0].length ||
                           dist > MAXDIST) return false;
                    }
                    
                    map[(int)(playerZ + eZ * dist)][(int)(playerY + eY * dist)][(int)(playerX + eX * dist)] = '.';
                    return true;
                }
                
                return false;
            }
            
            public boolean keyUp(int key) {
                if(key == Input.Keys.W) {
                    forward = false;
                    return true;
                } else if(key == Input.Keys.S) {
                    backward = false;
                    return true;
                } else if(key == Input.Keys.A) {
                    left = false;
                    return true;
                } else if(key == Input.Keys.D) {
                    right = false;
                    return true;
                } else if(key == Input.Keys.UP) {
                    camUp = false;
                    return true;
                } else if(key == Input.Keys.DOWN) {
                    camDown = false;
                    return true;
                } else if(key == Input.Keys.LEFT) {
                    camLeft = false;
                    return true;
                } else if(key == Input.Keys.RIGHT) {
                    camRight = false;
                    return true;
                }
                
                return false;
            }
            
            public boolean touchUp(int x, int y, int ptr, int btn) {
                touch.set(x, y);
                viewport.unproject(touch);
                
                if(touch.x < WIDTH / 2 && touch.x > 0 && Gdx.app.getType() == Application.ApplicationType.Android)
                    return keyUp(Input.Keys.W) && keyUp(Input.Keys.S) && keyUp(Input.Keys.A) && keyUp(Input.Keys.D);
                else if((touch.x > WIDTH / 2 && touch.x < WIDTH) || Gdx.app.getType() != Application.ApplicationType.Android)
                    return keyUp(Input.Keys.UP) && keyUp(Input.Keys.DOWN) && keyUp(Input.Keys.LEFT) && keyUp(Input.Keys.RIGHT);
                return false;
            }
            
            public boolean touchDown(int x, int y, int ptr, int btn) {
                touch.set(x, y);
                viewport.unproject(touch);
                
                if(Gdx.app.getType() == Application.ApplicationType.Android) {
                    if(touch.x > 0 && touch.x < WIDTH / 2 && touch.y > 150)
                        return keyDown(Input.Keys.E);
                    else if(touch.x > WIDTH / 2 && touch.x < WIDTH && touch.y > 150)
                        return keyDown(Input.Keys.Q);
                    else if(touch.x > 34 && touch.x < 68 && touch.y > 68 && touch.y < 102)
                        return keyDown(Input.Keys.W);
                    else if(touch.x > 34 && touch.x < 68 && touch.y > 0 && touch.y < 34)
                        return keyDown(Input.Keys.S);
                    else if(touch.x > 0 && touch.x < 34 && touch.y > 34 && touch.y < 68)
                        return keyDown(Input.Keys.A);
                    else if(touch.x > 68 && touch.x < 102 && touch.y > 34 && touch.y < 68)
                        return keyDown(Input.Keys.D);
                    else if(touch.x > 34 && touch.x < 68 && touch.y > 34 && touch.y < 68)
                        return keyDown(Input.Keys.SPACE);
                }
                
                startX = (int)touch.x;
                startY = (int)touch.y;
                
                return true;
            }
            
            public boolean touchDragged(int x, int y, int ptr) {
                touch.set(x, y);
                viewport.unproject(touch);
                
                if((touch.x > WIDTH / 2 && touch.x < WIDTH && touch.y < 150) || Gdx.app.getType() != Application.ApplicationType.Android) {
                    if(touch.x < startX - 10) {
                        camLeft  = true;
                        camRight = false;
                    } else if(touch.x > startX + 10) {
                        camRight = true;
                        camLeft  = false;
                    }
                    
                    if(touch.y < startY - 10) {
                        camDown = true;
                        camUp   = false;
                    } else if(touch.y > startY + 10) {
                        camUp   = true;
                        camDown = false;
                    }
                    return true;
                }
                
                return false;
            }
        });
        
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(20);
                        
                        if(forward) {
                            final float newX = playerX + (float)Math.cos(playerAngle) * 0.05f;
                            if((int)newX >= 0 && (int)newX < map[0][0].length && map[(int)playerZ][(int)playerY][(int)newX] == '.') playerX = newX;
                            final float newY = playerY + (float)Math.sin(playerAngle) * 0.05f;
                            if((int)newY >= 0 && (int)newY < map[0].length && map[(int)playerZ][(int)newY][(int)playerX] == '.') playerY = newY;
                        }
                        
                        if(backward) {
                            final float newX = playerX - (float)Math.cos(playerAngle) * 0.05f;
                            if((int)newX >= 0 && (int)newX < map[0][0].length && map[(int)playerZ][(int)playerY][(int)newX] == '.') playerX = newX;
                            final float newY = playerY - (float)Math.sin(playerAngle) * 0.05f;
                            if((int)newY >= 0 && (int)newY < map[0].length && map[(int)playerZ][(int)newY][(int)playerX] == '.') playerY = newY;
                        }
                        
                        if(right) {
                            final float newX = playerX + (float)Math.cos(playerAngle + 1.5707f) * 0.05f;
                            if((int)newX >= 0 && (int)newX < map[0][0].length && map[(int)playerZ][(int)playerY][(int)newX] == '.') playerX = newX;
                            final float newY = playerY + (float)Math.sin(playerAngle + 1.5707f) * 0.05f;
                            if((int)newY >= 0 && (int)newY < map[0].length && map[(int)playerZ][(int)newY][(int)playerX] == '.') playerY = newY;
                        }
                        
                        if(left) {
                            final float newX = playerX - (float)Math.cos(playerAngle + 1.5707f) * 0.05f;
                            if((int)newX >= 0 && (int)newX < map[0][0].length && map[(int)playerZ][(int)playerY][(int)newX] == '.') playerX = newX;
                            final float newY = playerY - (float)Math.sin(playerAngle + 1.5707f) * 0.05f;
                            if((int)newY >= 0 && (int)newY < map[0].length && map[(int)playerZ][(int)newY][(int)playerX] == '.') playerY = newY;
                        }
                        
                        if(camLeft) {
                            playerAngle -= 0.02f;
                            if(playerAngle <= 0.0f) playerAngle = 6.28318f;
                        }
                        
                        if(camRight) {
                            playerAngle += 0.02f;
                            if(playerAngle > 6.28318f) playerAngle = 0.0001f;
                        }
                        
                        if(camDown) {
                            playerAngleZ -= 0.02f;
                            if(playerAngleZ < -1.5707f) playerAngleZ = -1.5707f;
                        }
                        
                        if(camUp) {
                            playerAngleZ += 0.02f;
                            if(playerAngleZ > 1.5707f) playerAngleZ = 1.5707f;
                        }
                        
                        if(!jump && map[(int)playerZ - 1][(int)playerY][(int)playerX] == '.')
                            playerZ -= 0.05f;
                        else if(!jump)
                            playerZ = (int)playerZ + 0.7f;
                    } catch(Exception ex) {}
                }
            }
        }.start();
    }
    
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    public void pause() {
        if(!save) {
            save = true;
            new Thread() {
                public void run() {
                    String saveString = "";
                    for(int z = 0; z < map.length; z++) {
                        for(int y = 0; y < map[z].length; y++) {
                            for(int x = 0; x < map[z][y].length; x++)
                                saveString += "" + map[z][y][x];
                            saveString += "\n";
                        }
                    }
                    Gdx.files.local("map").writeString(saveString, false);
                    save = false;
                }
            }.start();
        }
    }

    public void resume() {}

    public void dispose() {}
    
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        viewport.getCamera().update();
        
        shape.setProjectionMatrix(viewport.getCamera().combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        
        shape.setColor(Color.CYAN);
        shape.rect(0, 0, WIDTH, HEIGHT);
        
        // ray casting
        for(float x = 0; x < WIDTH; x += 1.18f) {
            final float rayAngle = playerAngle - FOV / 2 + x * (FOV / WIDTH);
            final float eX = (float)Math.cos(rayAngle);
            final float eY = (float)Math.sin(rayAngle);
            
            yLoop:
            for(float y = 0; y < HEIGHT; y += 1.18f) {
                final float eZ = (float)Math.sin(playerAngleZ - VFOV / 2 + y * (VFOV / HEIGHT));
                
                float dist = 0.0f;
                while(map[(int)(playerZ + eZ * dist)][(int)(playerY + eY * dist)][(int)(playerX + eX * dist)] == '.') {
                    dist += 0.05f;
                    if((int)(playerZ + eZ * dist) < 0 || (int)(playerZ + eZ * dist) >= map.length ||
                       (int)(playerY + eY * dist) < 0 || (int)(playerY + eY * dist) >= map[0].length ||
                       (int)(playerX + eX * dist) < 0 || (int)(playerX + eX * dist) >= map[0][0].length ||
                       dist > MAXDIST) continue yLoop;
                }
                
                if((int)(playerZ + eZ * dist) != 0 && (int)(playerX + eX * (dist - 0.05f)) != (int)(playerX + eX * dist))
                    shape.setColor(new Color(1, 1, 1, 1));
                else if((int)(playerZ + eZ * dist) != 0 && (int)(playerZ + eZ * (dist - 0.05f)) != (int)(playerZ + eZ * dist))
                    shape.setColor(new Color(0.95f, 0.95f, 0.95f, 1));
                else if((int)(playerZ + eZ * dist) != 0)
                    shape.setColor(new Color(0.9f, 0.9f, 0.9f, 1));
                else
                    shape.setColor(new Color(0, 0.5f, 0, 1));
                shape.rect(x, y, 1.18f, 1.18f);
            }
        }
        
        shape.setColor(Color.BLUE);
        shape.ellipse(WIDTH / 2 - 0.5f, HEIGHT / 2 - 0.5f, 1, 1);
        if(Gdx.app.getType() == Application.ApplicationType.Android) {
            shape.setColor(Color.BLACK);
            shape.ellipse(34, 0, 34, 34);
            shape.ellipse(0, 34, 34, 34);
            shape.ellipse(68, 34, 34, 34);
            shape.ellipse(34, 68, 34, 34);
        }
        
        shape.end();
    }
}
