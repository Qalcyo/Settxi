/*
 | EvergreenHUD - A mod to improve on your heads-up-display.
 | Copyright (C) isXander [2019 - 2021]
 |
 | This program comes with ABSOLUTELY NO WARRANTY
 | This is free software, and you are welcome to redistribute it
 | under the certain conditions that can be found here
 | https://www.gnu.org/licenses/lgpl-3.0.en.html
 |
 | If you have any questions or concerns, please create
 | an issue on the github page that can be found here
 | https://github.com/isXander/EvergreenHUD
 |
 | If you have a private concern, please contact
 | isXander @ business.isxander@gmail.com
 */

package dev.isxander.settxi.serialization

import com.electronwill.nightconfig.core.Config
import dev.isxander.settxi.Setting
import dev.isxander.settxi.SettingAdapter
import dev.isxander.settxi.exception.SettxiException
import dev.isxander.settxi.impl.*
import dev.isxander.settxi.providers.AdapterProvider
import dev.isxander.settxi.providers.PropertyProvider
import dev.isxander.settxi.utils.DataTypes
import java.awt.Color
import java.lang.ClassCastException
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

interface ConfigProcessor {
    var conf: Config

    fun addSettingToConfig(setting: Setting<*, *>, data: Config): Config {
        var key = "${setting.category}."
        if (setting.subcategory != "") key += "${setting.subcategory}."
        key += setting.nameSerializedKey

        when (setting.dataType) {
            DataTypes.String -> data.set<String>(key, setting.serializedValue as String)
            DataTypes.Boolean -> data.set<Boolean>(key, setting.serializedValue as Boolean)
            DataTypes.Float -> data.set<Float>(key, setting.serializedValue as Float)
            DataTypes.Double -> data.set<Double>(key, setting.serializedValue as Double)
            DataTypes.Int -> data.set<Int>(key, setting.serializedValue as Int)
        }
        return data
    }

    fun setSettingFromConfig(data: Config, setting: Setting<*, *>) {
        var key = "${setting.category}."
        if (setting.subcategory != "") key += "${setting.subcategory}."
        key += setting.nameSerializedKey

        setting.serializedValue = when (setting.dataType) {
            DataTypes.Boolean -> data[key] ?: setting.defaultSerializedValue as Boolean
            DataTypes.Double -> data[key] ?: setting.defaultSerializedValue as Double
            DataTypes.Float -> data.get<Double>(key)?.toFloat() ?: setting.defaultSerializedValue as Float
            DataTypes.Int -> data[key] ?: setting.defaultSerializedValue as Int
            DataTypes.String -> data[key] ?: setting.defaultSerializedValue as String
        }
    }

    fun collectSettings(instance: Any, settingProcessor: (Setting<*, *>) -> Unit) {
        val classes = arrayListOf(instance::class)
        classes.addAll(instance::class.allSuperclasses)
        for (declaredClass in classes) {
            for (property in declaredClass.declaredMemberProperties) {
                property.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                try {
                    when {
                        property.hasAnnotation<BooleanSetting>() ->
                            settingProcessor.invoke(BooleanSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Boolean>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Boolean>
                                    )
                            ))
                        property.hasAnnotation<ColorSetting>() ->
                            settingProcessor.invoke(ColorSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Color>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Color>
                                    )
                            ))
                        property.hasAnnotation<OptionSetting>() ->
                            settingProcessor.invoke(OptionSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<OptionContainer.Option>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, OptionContainer.Option>
                                    )
                            ))
                        property.hasAnnotation<FloatSetting>() ->
                            settingProcessor.invoke(FloatSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Float>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Float>
                                    )
                            ))
                        property.hasAnnotation<DoubleSetting>() ->
                            settingProcessor.invoke(DoubleSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Double>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Double>
                                    )
                            ))
                        property.hasAnnotation<IntSetting>() ->
                            settingProcessor.invoke(IntSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Int>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Int>
                                    )
                            ))
                        property.hasAnnotation<StringListSetting>() ->
                            settingProcessor.invoke(StringListSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<String>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, String>
                                    )
                            ))
                        property.hasAnnotation<StringSetting>() ->
                            settingProcessor.invoke(StringSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<String>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, String>
                                    )
                            ))
                    }
                } catch (e: ClassCastException) {
                    throw SettxiException("${property.name} is incorrect type in ${declaredClass.qualifiedName}", e)
                }
            }
        }
    }

}