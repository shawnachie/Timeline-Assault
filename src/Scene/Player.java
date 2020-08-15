package Scene;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Game.LevelState;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Utils.AirGroundState;
import Utils.Direction;
import Utils.MathUtils;
import GameObject.Rectangle;
import Utils.Timer;

import java.util.ArrayList;

public abstract class Player extends GameObject {
    protected float walkSpeed = 0;
    protected float gravity = 0;
    protected float jumpHeight = 0;
    protected float jumpDegrade = 0;
    protected float terminalVelocityY = 0;
    protected float momentumYIncrease = 0;
    protected float jumpForce = 0;
    protected float momentumY = 0;
    protected float moveAmountX, moveAmountY;
    protected PlayerState playerState;
    protected PlayerState previousPlayerState;
    protected Direction facingDirection;
    protected AirGroundState airGroundState;
    protected AirGroundState previousAirGroundState;
    protected KeyLocker keyLocker = new KeyLocker();
    protected Key JUMP_KEY = Key.UP;
    protected Key MOVE_LEFT_KEY = Key.LEFT;
    protected Key MOVE_RIGHT_KEY = Key.RIGHT;
    protected Key CROUCH_KEY = Key.DOWN;
    protected ArrayList<PlayerListener> listeners = new ArrayList<>();
    protected LevelState levelState;

    public Player(SpriteSheet spriteSheet, float x, float y, String startingAnimationName, Map map) {
        super(spriteSheet, x, y, startingAnimationName, map);
        facingDirection = Direction.RIGHT;
        airGroundState = AirGroundState.AIR;
        previousAirGroundState = airGroundState;
        playerState = PlayerState.STANDING;
        previousPlayerState = playerState;
        levelState = LevelState.RUNNING;
    }

    public void update(Keyboard keyboard, Map map) {
        moveAmountX = 0;
        moveAmountY = 0;

        if (levelState == LevelState.RUNNING) {
            applyGravity();

            do {
                previousPlayerState = playerState;
                handlePlayerState(keyboard);
            } while (previousPlayerState != playerState);

            previousAirGroundState = airGroundState;

            super.update();

            super.moveYHandleCollision(map, moveAmountY);
            super.moveXHandleCollision(map, moveAmountX);

            updateLockedKeys(keyboard);
        } else if (levelState == LevelState.LEVEL_COMPLETED) {
            levelCompleted(map);
            super.update();
        } else if (levelState == LevelState.PLAYER_DEAD) {
            playerDead(map);
            super.update();
        }
    }

    protected void applyGravity() {
        moveAmountY += gravity + momentumY;
    }

    protected void handlePlayerState(Keyboard keyboard) {
        switch (playerState) {
            case STANDING:
                playerStanding(keyboard);
                break;
            case WALKING:
                playerWalking(keyboard);
                break;
            case CROUCHING:
                playerCrouching(keyboard);
                break;
            case JUMPING:
                playerJumping(keyboard);
                break;
        }
    }

    protected void playerStanding(Keyboard keyboard) {
        currentAnimationName = facingDirection == Direction.RIGHT ? "STAND_RIGHT" : "STAND_LEFT";
        if (keyboard.isKeyDown(MOVE_LEFT_KEY) || keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.WALKING;
        } else if (keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        } else if (keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    protected void playerWalking(Keyboard keyboard) {
        currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        if (keyboard.isKeyDown(MOVE_LEFT_KEY)) {
            moveAmountX -= walkSpeed;
            facingDirection = Direction.LEFT;
        } else if (keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
            moveAmountX += walkSpeed;
            facingDirection = Direction.RIGHT;
        } else if (keyboard.isKeyUp(MOVE_LEFT_KEY) && keyboard.isKeyUp(MOVE_RIGHT_KEY)) {
            playerState = PlayerState.STANDING;
        }

        if (keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        } else if (keyboard.isKeyDown(CROUCH_KEY)) {
            playerState = PlayerState.CROUCHING;
        }
    }

    protected void playerCrouching(Keyboard keyboard) {
        currentAnimationName = facingDirection == Direction.RIGHT ? "CROUCH_RIGHT" : "CROUCH_LEFT";
        if (keyboard.isKeyUp(CROUCH_KEY)) {
            playerState = PlayerState.STANDING;
        }
        if (keyboard.isKeyDown(JUMP_KEY) && !keyLocker.isKeyLocked(JUMP_KEY)) {
            keyLocker.lockKey(JUMP_KEY);
            playerState = PlayerState.JUMPING;
        }
    }

    protected void playerJumping(Keyboard keyboard) {
        if (previousAirGroundState == AirGroundState.GROUND && airGroundState == AirGroundState.GROUND) {
            currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            airGroundState = AirGroundState.AIR;
            jumpForce = jumpHeight;
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }
        }
        else if (airGroundState == AirGroundState.AIR) {
            if (jumpForce > 0) {
                moveAmountY -= jumpForce;
                jumpForce -= jumpDegrade;
                if (jumpForce < 0) {
                    jumpForce = 0;
                }
            }

            if (previousY > Math.round(y)) {
                currentAnimationName = facingDirection == Direction.RIGHT ? "JUMP_RIGHT" : "JUMP_LEFT";
            } else {
                currentAnimationName = facingDirection == Direction.RIGHT ? "FALL_RIGHT" : "FALL_LEFT";
            }

            if (keyboard.isKeyDown(MOVE_LEFT_KEY)) {
                moveAmountX -= walkSpeed;
            } else if (keyboard.isKeyDown(MOVE_RIGHT_KEY)) {
                moveAmountX += walkSpeed;
            }

            if (moveAmountY > 0) {
                increaseMomentum();
            }
        }
        else if (previousAirGroundState == AirGroundState.AIR && airGroundState == AirGroundState.GROUND) {
            playerState = PlayerState.STANDING;
        }
    }

    protected void increaseMomentum() {
        momentumY += momentumYIncrease;
        if (momentumY > terminalVelocityY) {
            momentumY = terminalVelocityY;
        }
    }

    protected void updateLockedKeys(Keyboard keyboard) {
        if (keyboard.isKeyUp(JUMP_KEY)) {
            keyLocker.unlockKey(JUMP_KEY);
        }
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction) {

    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction) {
        if (direction == Direction.DOWN) {
            if (hasCollided) {
                momentumY = 0;
                airGroundState = AirGroundState.GROUND;
            } else {
                playerState = PlayerState.JUMPING;
                airGroundState = AirGroundState.AIR;
            }
        } else if (direction == Direction.UP) {
            if (hasCollided) {
                jumpForce = 0;
            }
        }
    }

    public void hurt(MapEntity mapEntity) {
        if (mapEntity instanceof Enemy) {
            levelState = LevelState.PLAYER_DEAD;
        }
    }

    public void levelCompleted(Map map) {
        if (airGroundState != AirGroundState.GROUND) {
            currentAnimationName = "FALL_RIGHT";
            applyGravity();
            increaseMomentum();
            moveYHandleCollision(map, moveAmountY);
        }
        else if (map.getCamera().containsDraw(this)) {
            currentAnimationName = "WALK_RIGHT";
            moveXHandleCollision(map, walkSpeed);
        } else {
            for (PlayerListener listener : listeners) {
                listener.onLevelCompleted();
            }
        }
    }

    public void playerDead(Map map) {
        if (!currentAnimationName.startsWith("DEATH")) {
            if (facingDirection == Direction.RIGHT) {
                currentAnimationName = "DEATH_RIGHT";
            } else {
                currentAnimationName = "DEATH_LEFT";
            }
        } else if (currentFrameIndex == getCurrentAnimation().length - 1) {
            if (map.getCamera().containsDraw(this)) {
                applyGravity();
                increaseMomentum();
                moveY(moveAmountY + momentumY);
            } else {
                for (PlayerListener listener : listeners) {
                    listener.onDeath();
                }
            }
        }
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    public AirGroundState getAirGroundState() {
        return airGroundState;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }
}