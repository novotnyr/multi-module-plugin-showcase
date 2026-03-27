package com.github.novotnyr.mincssrel.css

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages

class CssAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInfoMessage("An action from a CSS content module was invoked", "Mincssrel")
    }
}