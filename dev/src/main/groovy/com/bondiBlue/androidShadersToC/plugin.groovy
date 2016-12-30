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
				filesNameConv = project.shadersConf.filesNameConv
				openBracketSameLine = project.shadersConf.openBracketSameLine
				structPrefix = project.shadersConf.structPrefix
				structSuffix = project.shadersConf.structSuffix
				structNameConv = project.shadersConf.structNameConv
				varsNameConv = project.shadersConf.varsNameConv
				staticVarsPrefix = project.shadersConf.staticVarsPrefix
				staticVarsSuffix = project.shadersConf.staticVarsSuffix
				staticVarsNameConv = project.shadersConf.staticVarsNameConv
				packetNamePrefix = project.shadersConf.packetNamePrefix
				packetNameSuffix = project.shadersConf.packetNameSuffix
				shaderAddFlavourToVarName = project.shadersConf.shaderAddFlavourToVarName
				functionsNameConv = project.shadersConf.functionsNameConv
				functionNewPrefix = project.shadersConf.functionNewPrefix
				functionNewSuffix = project.shadersConf.functionNewSuffix
				functionDeletePrefix = project.shadersConf.functionDeletePrefix
				functionDeleteSuffix = project.shadersConf.functionDeleteSuffix
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
