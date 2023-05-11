package com.github.mikrsie2.analyz.toolWindow

import com.github.mikrsie2.analyz.MyBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import javax.swing.JButton
import javax.swing.JLabel

class MyToolWindowFactory : ToolWindowFactory {
    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project)
        val content = contentFactory.createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    inner class MyToolWindow(private val project: Project) {
        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val info = JLabel()
            info.text = getCurrentPsiInfo()
            add(info)
            add(JButton(MyBundle.message("refresh")).apply {
                addActionListener {
                    info.text = getCurrentPsiInfo()
                }
            })
        }

        private fun getCurrentPsiInfo(): String {
            val builder = StringBuilder()
            getAllJavaFiles(project).forEach { psiFile ->
                var classes = 0
                var methods = 0
                psiFile.accept(object :
                    JavaRecursiveElementVisitor() {
                    override fun visitClass(aClass: PsiClass) {
                        super.visitClass(aClass)
                        classes++
                        methods += aClass.methods.size
                    }
                })
                builder.append(MyBundle.message("row", psiFile.name, classes, methods))
            }
            return MyBundle.message("table", MyBundle.message("style"), builder.toString())
        }
    }

    fun getAllJavaFiles(project: Project): List<PsiFile> {
        val psiManager = PsiManager.getInstance(project)
        val javaFiles = mutableListOf<PsiFile>()
        val fileIndex = ProjectFileIndex.getInstance(project)
        val javaFileNames =
            FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.allScope(project))
        val sourceJavaFiles = javaFileNames.filter { kotlinFile ->
            fileIndex.isInSourceContent(kotlinFile)
        }
        for (javaFile in sourceJavaFiles) {
            val psiFile = psiManager.findFile(javaFile)
            if (psiFile != null) {
                javaFiles.add(psiFile)
            }
        }
        return javaFiles
    }
}
