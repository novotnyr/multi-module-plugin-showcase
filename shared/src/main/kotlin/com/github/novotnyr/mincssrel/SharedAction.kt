package com.github.novotnyr.mincssrel

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages

internal class SharedAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Messages.showInfoMessage("An action from a shared content module was invoked", "Mincssrel")
    }
}