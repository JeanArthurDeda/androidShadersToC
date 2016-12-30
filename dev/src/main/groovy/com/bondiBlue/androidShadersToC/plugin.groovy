package com.bondiBlue.androidShadersToC

import org.gradle.api.Project
import org.gradle.api.Plugin

class Plugin implements Plugin<Project> {

	void apply(Project project) {
		project.extensions.create("shadersConf", TaskInput)

		project.afterEvaluate {
			project.task('shadersBuild', type: Task) {
				description 'Generates C code with embedded androids shaders.'
				inputDir = project.shadersConf.inputDir
				outputDir = project.shadersConf.outputDir
				fileNameConv = project.shadersConf.fileNameConv
				openBracketSameLine = project.shadersConf.openBracketSameLine
				structPrefix = project.shadersConf.structPrefix
				structSuffix = project.shadersConf.structSuffix
				structNameConv = project.shadersConf.structNameConv
				varNameConv = project.shadersConf.varNameConv
				staticVarPrefix = project.shadersConf.staticVarPrefix
				staticVarSuffix = project.shadersConf.staticVarSuffix
				staticVarNameConv = project.shadersConf.staticVarNameConv
				packetPrefix = project.shadersConf.packetPrefix
				packetSuffix = project.shadersConf.packetSuffix
				shaderAddFlavourToVarName = project.shadersConf.shaderAddFlavourToVarName
				funcNameConv = project.shadersConf.funcNameConv
				funcNewPrefix = project.shadersConf.funcNewPrefix
				funcNewSuffix = project.shadersConf.funcNewSuffix
				funcDeletePrefix = project.shadersConf.funcDeletePrefix
				functDeleteSuffix = project.shadersConf.functDeleteSuffix
				templateDir = project.shadersConf.templateDir
				writeTemplates = project.shadersConf.writeTemplates
				wc = project.shadersConf.wc
				wh = project.shadersConf.wh
				wGLint = project.shadersConf.wGLint
				wGLuint = project.shadersConf.wGLuint
			}
			project.task('shadersClean') {
				description 'Deletes the generated C code for android shaders.'
				doFirst{
					project.delete (project.shadersConf.outputDir.listFiles ());
				}
			}
		}
	}
}
