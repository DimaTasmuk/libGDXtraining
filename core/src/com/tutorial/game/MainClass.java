package com.tutorial.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

class MainClass extends ApplicationAdapter {
	private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture sprite;
    private Rectangle player;
    private Array<Rectangle> enemies, shots;
    private Sound explosionSound;
    private int width, height;
    private TextureRegion playerRegion, enemyRegion, shotRegion;
    private long lastEnemyTime, lastShotTime;
    private Vector3 touchPos;

	
	@Override
	public void create () {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
		batch = new SpriteBatch();
        sprite = new Texture("1945.png");
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        //
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width, height);
        //
        playerRegion = new TextureRegion(sprite, 1595, 11, 188, 188);
        enemyRegion = new TextureRegion(sprite, 1397, 209, 188, 188);
        shotRegion = new TextureRegion(sprite, 167, 429, 14, 14);

        player = new Rectangle();
        player.x = (width - playerRegion.getRegionWidth()) / 2;
        player.y = 0;
        player.width = 188;
        player.height = 188;

        enemies = new Array<Rectangle>();
        shots = new Array<Rectangle>();
        spawnEnemy();
        doShot();
	}


	private void spawnEnemy() {
        Rectangle enemy = new Rectangle();
        enemy.x = MathUtils.random(0, width - enemyRegion.getRegionWidth());
        enemy.y = height - enemyRegion.getRegionHeight();
        enemy.width = 188;
        enemy.height = 188;
        enemies.add(enemy);
        lastEnemyTime = TimeUtils.millis();
    }

    private void doShot() {
        Rectangle shot = new Rectangle();
        shot.x = player.x + playerRegion.getRegionWidth() / 2;
        shot.y = player.y + playerRegion.getRegionHeight();
        shot.width = 14;
        shot.height = 14;
        shots.add(shot);
        lastShotTime = TimeUtils.millis();
    }

	@Override
	public void render () {
        float deltaTime = Gdx.graphics.getDeltaTime();
		Gdx.gl.glClearColor(0, (float)67/255, (float)171/255, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(playerRegion, player.x, player.y);
        for (Rectangle enemy : enemies) {
            batch.draw(enemyRegion, enemy.x, enemy.y);
        }
        for (Rectangle shot : shots) {
            batch.draw(shotRegion, shot.x, shot.y);
        }
		batch.end();

        if (Gdx.input.isTouched()) {
            touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            player.x = touchPos.x - playerRegion.getRegionWidth() / 2;
        }

        if (player.x < 0 ) player.x = 0;
        if (player.x > width - playerRegion.getRegionWidth()) player.x = width - playerRegion.getRegionWidth();

        if (TimeUtils.millis() - lastEnemyTime > 1000) spawnEnemy();
        if (TimeUtils.millis() - lastShotTime > 500) doShot();

        Iterator<Rectangle> shotIter = shots.iterator();
        while (shotIter.hasNext()) {
            Rectangle shot = shotIter.next();
            shot.y += 500 * deltaTime;
            if (shot.y > height) shotIter.remove();

            Iterator<Rectangle> iter = enemies.iterator();
            while (iter.hasNext()) {
                Rectangle enemy = iter.next();
                enemy.y -= 100 * deltaTime;
                if (shot.overlaps(enemy)) {
                    shotIter.remove();
                    iter.remove();
                    explosionSound.play();
                    continue;
                }
                if (enemy.overlaps(player)) {
//                    explosionSound.play();
                    iter.remove();
                    continue;
                }
                if (enemy.y + 188 < 0) {
                    iter.remove();
                }
            }
        }
	}
	
	@Override
	public void dispose() {
		batch.dispose();
        sprite.dispose();
        explosionSound.dispose();
	}
}
