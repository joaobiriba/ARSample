package com.laquysoft.arsample

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            addObject(Uri.parse("TUI 787.sfb"))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addObject(model: Uri) {
        val frame = (sceneformFragment as ArFragment).arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject((sceneformFragment as ArFragment), hit.createAnchor(), model)
                    break

                }
            }
        }
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        val renderableFuture = ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()

        val otherRenderableFuture = ModelRenderable.builder()
                .setSource(fragment.context, model)
                .build()

        GlobalScope.future(Dispatchers.Main, CoroutineStart.DEFAULT, {
            try {
                addNodeToScene(fragment, anchor, renderableFuture.await(), otherRenderableFuture.await())
            } catch (e: Throwable) {
                AlertDialog.Builder(this@MainActivity)
                        .setMessage(e.message)
                        .setTitle("Codelab error!")
                        .create()
                        .show()
            }
        })
    }

    private fun addNodeToScene(fragment: ArFragment, anchora: Anchor, renderable: Renderable, otherRenderable: Renderable) {
        val scene = scene {
            anchorNode {
                anchor = anchora
                node {
                    transformationSystem = fragment.transformationSystem
                    model = renderable
                }
            }
            node {
                transformationSystem = fragment.transformationSystem
                model = otherRenderable
            }
        }
        fragment.arSceneView.scene.addChild(scene.nodes.first())
    }

    private fun getScreenCenter(): android.graphics.Point {
        val vw = findViewById<View>(android.R.id.content)
        return android.graphics.Point(vw.width / 2, vw.height / 2)
    }
}
