package io.livstream

import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.RecyclerView

import android.support.v7.widget.helper.ItemTouchHelper
import android.R.attr.button
import android.R.attr.centerY
import android.R.attr.centerX
import android.graphics.Color


// from https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28

interface SwipeControllerActions {

    fun onLeftSwipe(position: Int) {}

    fun onRightSwipe(position: Int) {}
}

internal class SwipeController(swipeActions: SwipeControllerActions) : ItemTouchHelper.Callback() {

    val swipeActions = swipeActions

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return ItemTouchHelper.Callback.makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.layoutPosition
        if (direction == ItemTouchHelper.LEFT) {
            swipeActions.onLeftSwipe(position)
        } else if (direction == ItemTouchHelper.RIGHT) {
            swipeActions.onRightSwipe(position)
        }
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder?.itemView
        if (itemView != null) {
            val p = Paint()
            val textSize = 60f
            p.color = Color.RED
            p.isAntiAlias = true
            p.textSize = textSize
            val delText = "⏮ DELETE 🙁"
            val textWidth = p.measureText(delText)
            c?.drawText(delText, itemView.right.toFloat()-textWidth, textSize / 2f + (itemView.top + itemView.bottom) / 2f, p)

            p.color = Color.GREEN
            val doneText = "😀 DONE ⏭"
            c?.drawText(doneText, itemView.left.toFloat(), textSize / 2f + (itemView.top + itemView.bottom) / 2f, p)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}