package io.github.chengzis.chain

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.github.chengzis.ksp.ICodeGenerator

class ChainGenerator(private val metadata: ChainMetadata) : ICodeGenerator<FileSpec> {

    @OptIn(KspExperimental::class)
    override fun generate(environment: SymbolProcessorEnvironment): FileSpec {
        val builder = TypeSpec.classBuilder(metadata.className)
            .addModifiers(KModifier.ABSTRACT, KModifier.PUBLIC)
            .addSuperinterface(metadata.defineClassName)

        try {
            val extend = metadata.chain.extends.asClassName()
            if (extend != UNIT) {
                builder.superclass(ChainMetadata.getTargetClassName(extend))
            }
        } catch (e: KSTypeNotPresentException) {
            val extend = e.ksType.toClassName()
            builder.superclass(ChainMetadata.getTargetClassName(extend))
        }

        metadata.doc?.let {
            builder.addKdoc(it)
            builder.addKdoc("\n")
        }
        builder.addKdoc("@see %T", metadata.defineClassName)

        builder.addProperty(
            PropertySpec.builder(metadata.interceptorsPropertyName, metadata.interceptorsClassName)
                .addModifiers(KModifier.PRIVATE)
                .addKdoc("拦截器")
                .initializer("${metadata.interceptorsClassName.simpleName}()")
                .build()
        )

        builder.addType(ArgsGenerator(metadata).generate(environment))
        builder.addType(InterceptorsGenerator(metadata).generate(environment))
        builder.addFunctions(ExFuncGenerator(metadata).generate(environment))
        builder.addFunctions(OverrideFuncGenerator(metadata).generate(environment))
        builder.addFunctions(AddInterceptorGenerator(metadata).generate(environment))


        return FileSpec.builder(metadata.className)
            .addType(builder.build())
            .apply {
                for (funSpec in AddInterceptor2Generator(metadata).generate(environment)) {
                    addFunction(funSpec)
                }
            }.build()
    }

}