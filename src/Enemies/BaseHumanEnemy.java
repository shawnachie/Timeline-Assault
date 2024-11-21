package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Enemy;
import Level.MapEntity;
import Level.Player;
import Utils.AirGroundState;
import Utils.Point;
import Utils.Direction;
import java.util.HashMap;
import java.util.Random;

// This class is for the base human enemy that shoots bullets straight in whichever direction it is facing
public class BaseHumanEnemy extends Enemy {

    private float gravity = .5f;
    private float movementSpeed = 1.25f;
    private Direction startFacingDirection;
    private Direction facingDirection;
    private AirGroundState airGroundState;
    private HumanState currentState;
    private int chaseDelayTimer = 60; // Timer for delaying the chase state
    private int shootWaitTimer = 65;
    private int shootTimer;

    protected Point startLocation;
    protected Point endLocation;

    protected HumanState humanState;
    protected HumanState previousHumanState;

    public BaseHumanEnemy(Point location, Direction facingDirection) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("ZombieTrial.png"), 63, 58), "WALK_LEFT");
        this.startFacingDirection = facingDirection;
        this.hitPoints = 3;
        this.currentState = HumanState.WALK;
        this.initialize();
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.startFacingDirection = facingDirection;
    }

    @Override
    public void initialize() {
        super.initialize();
        facingDirection = startFacingDirection;
        previousHumanState = humanState;
        if (facingDirection == Direction.RIGHT) {
            currentAnimationName = "WALK_RIGHT";
        } else if (facingDirection == Direction.LEFT) {
            currentAnimationName = "WALK_LEFT";
        }
        airGroundState = AirGroundState.GROUND;

        shootWaitTimer = 65;
    }

    @Override
    public void update(Player player) {
        float moveAmountX = 0;
        float moveAmountY = 0;

        // Determine distance between enemy and player
        float distanceToPlayer = player.getX() - getX();

        // Chase logic with delay: If the player is within a certain distance, start chasing after a delay
        if (Math.abs(distanceToPlayer) < 500) { // Adjust 500 as per the range you want
            if (chaseDelayTimer == 0) {
                currentState = HumanState.CHASE;
            } else {
                chaseDelayTimer--;
            }
        } else {
            currentState = HumanState.WALK;
            chaseDelayTimer = 60; // Reset delay timer when not in chase range
        }

        // Movement logic
        if (currentState == HumanState.WALK || currentState == HumanState.CHASE) {
            if (currentState == HumanState.CHASE) {
                // Adjust facing direction towards player
                facingDirection = distanceToPlayer > 0 ? Direction.RIGHT : Direction.LEFT;
            }

            if (airGroundState == AirGroundState.GROUND) {
                moveAmountX += (facingDirection == Direction.RIGHT ? movementSpeed : -movementSpeed);
            }
        }

        // Shooting logic
        if (shootWaitTimer == 0 && humanState != HumanState.SHOOT_WAIT) {
            humanState = HumanState.SHOOT_WAIT;
        } else {
            shootWaitTimer--;
        }

        if (humanState == HumanState.SHOOT_WAIT) {
            if (previousHumanState == HumanState.WALK) {
                shootTimer = 65;
                currentAnimationName = facingDirection == Direction.RIGHT ? "SHOOT_RIGHT" : "SHOOT_LEFT";
            } else if (shootTimer == 0) {
                humanState = HumanState.SHOOT;
            } else {
                shootTimer--;
            }
        }

        if (humanState == HumanState.SHOOT) {
            int bulletX;
            float bulletSpeed;
            if (facingDirection == Direction.RIGHT) {
                bulletX = Math.round(getX()) + getWidth();
                bulletSpeed = 3;
            } else {
                bulletX = Math.round(getX() - 21);
                bulletSpeed = -3;
            }

            int bulletY = Math.round(getY()) + 4;

            // Create bullet with updated constructor
            HumanEnemyProjectiles bullet = new HumanEnemyProjectiles(
                new Point(bulletX, bulletY),
                bulletSpeed,
                300, player = null
            );

            map.addEnemy(bullet);
            humanState = HumanState.WALK;
            shootWaitTimer = 130;
        }

        // Apply gravity and movement
        moveAmountY += gravity;
        moveYHandleCollision(moveAmountY);
        moveXHandleCollision(moveAmountX);

        super.update(player);
    }

    @Override
    public void onEndCollisionCheckX(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // if enemy has collided into something while walking forward,
        // it turns around (changes facing direction)
        if (hasCollided) {
            facingDirection = (direction == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
            currentAnimationName = facingDirection == Direction.RIGHT ? "WALK_RIGHT" : "WALK_LEFT";
        }
    }

    @Override
    public void onEndCollisionCheckY(boolean hasCollided, Direction direction, MapEntity entityCollidedWith) {
        // if enemy is colliding with the ground, change its air ground state to GROUND
        // if it is not colliding with the ground, it means that it's currently in the
        // air, so its air ground state is changed to AIR
        if (direction == Direction.DOWN) {
            airGroundState = hasCollided ? AirGroundState.GROUND : AirGroundState.AIR;
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0,0))
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build()
            });

            put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0,0))
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build()
            });

            put("WALK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0,1),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,2),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,3),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,4),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,5),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,6),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,7),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,8),25)
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build()
            });

            put("WALK_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0,1),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,2),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,3),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,4),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,5),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,6),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,7),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build(),
                new FrameBuilder(spriteSheet.getSprite(0,8),25)
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build()
            });

            put("SHOOT_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1,0))
                        .withScale(1)
                        .withBounds(20,20,20,20)
                        .build()
            });

            put("SHOOT_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(1,0))
                        .withScale(1)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .withBounds(20,20,20,20)
                        .build()
            });
        }};
    }

    public enum HumanState {
        WALK, SHOOT_WAIT, SHOOT, CHASE
    }
}
