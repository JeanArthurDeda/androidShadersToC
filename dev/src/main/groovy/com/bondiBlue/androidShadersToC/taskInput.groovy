package com.bondiBlue.androidShadersToC


class TaskInput {
	def File inputDir
	def File outputDir
	def String filesNameConv = 'lowerCamelCase'
	def boolean openBracketSameLine = false
	def String structPrefix = 't'
	def String structSuffix = ''
	def String structNameConv = 'lowerCamelCase'
	def String varsNameConv = 'lowerCamelCase'
	def String staticVarsPrefix = ''
	def String staticVarsSuffix = 's'
	def String staticVarsNameConv = 'goOutAnd_PLAY'
	def String packetNamePrefix = ''
	def String packetNameSuffix = 'shaders'
	def boolean shaderAddFlavourToVarName = true
	def String functionsNameConv = 'lowerCamelCase'
	def String functionNewPrefix = ''
	def String functionNewSuffix = 'new'
	def String functionDeletePrefix = ''
	def String functionDeleteSuffix = 'delete'
	String wc	= 'c'
	String wh	= 'h'
	String wGLint = 'GLint'
	String wGLuint = 'GLuint'
}
