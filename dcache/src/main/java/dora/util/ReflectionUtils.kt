package dora.util

import java.lang.reflect.*

object ReflectionUtils {
    // <editor-folder desc="Java类、方法、属性的创建和基本操作">
    fun newClass(className: String?): Class<*>? {
        return try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun findClass(className: String?): Boolean {
        val hasClass: Boolean
        hasClass = try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
        return hasClass
    }

    fun newInstance(className: String?): Any? {
        val clazz = newClass(className)
        return clazz?.let { newInstance(it) }
    }

    fun <T> newInstance(clazz: Class<T>): T? {
        val constructors = clazz.declaredConstructors
        for (c in constructors) {
            c.isAccessible = true
            val cls = c.parameterTypes
            if (cls.size == 0) {
                try {
                    return c.newInstance() as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            } else {
                val objs = arrayOfNulls<Any>(cls.size)
                for (i in cls.indices) {
                    objs[i] = getPrimitiveDefaultValue(cls[i])
                }
                try {
                    return c.newInstance(*objs) as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    fun newMethod(clazz: Class<*>, isDeclared: Boolean, methodName: String?, vararg parameterTypes: Class<*>?): Method? {
        try {
            return if (isDeclared) {
                clazz.getDeclaredMethod(methodName, *parameterTypes)
            } else {
                clazz.getMethod(methodName, *parameterTypes)
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
        return null
    }

    fun invokeMethod(obj: Any?, method: Method, vararg objects: Any?): Any? {
        method.isAccessible = true
        try {
            return method.invoke(obj, *objects)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    fun newField(clazz: Class<*>, isDeclared: Boolean, fieldName: String?): Field? {
        try {
            return if (isDeclared) {
                clazz.getDeclaredField(fieldName)
            } else {
                clazz.getField(fieldName)
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        return null
    }

    fun getFieldValue(field: Field, obj: Any?): Any? {
        field.isAccessible = true
        try {
            return field[obj]
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    fun setFieldValue(field: Field, value: Any?) {
        field.isAccessible = true
        try {
            field[field] = value
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    fun getStaticFieldValue(field: Field): Any {
        field.isAccessible = true
        if (isStaticField(field)) {
            try {
                return field[null]
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        throw RuntimeException("Field is not static.")
    }

    fun setStaticFieldValue(field: Field, value: Any?) {
        field.isAccessible = true
        if (isStaticField(field)) {
            try {
                field[null] = value
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        throw RuntimeException("Field is not static.")
    }

    // </editor-folder>
    // <editor-folder desc="判断属性是否有某关键字">
    fun isStaticField(f: Field): Boolean {
        return Modifier.isStatic(f.modifiers)
    }

    fun isFinalField(f: Field): Boolean {
        return Modifier.isFinal(f.modifiers)
    }

    fun isSynchronizedField(f: Field): Boolean {
        return Modifier.isSynchronized(f.modifiers)
    }

    fun isAbstract(f: Field): Boolean {
        return Modifier.isAbstract(f.modifiers)
    }

    fun isNative(f: Field): Boolean {
        return Modifier.isNative(f.modifiers)
    }

    fun isVolatile(f: Field): Boolean {
        return Modifier.isVolatile(f.modifiers)
    }

    fun isTransient(f: Field): Boolean {
        return Modifier.isTransient(f.modifiers)
    }

    // </editor-folder>
    fun getGenericType(obj: Any): Class<*> {
        return if (obj.javaClass.genericSuperclass is ParameterizedType &&
                (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.size > 0) {
            (obj.javaClass
                    .genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        } else (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }

    fun getGenericType(field: Field): Class<*>? {
        var type = field.genericType
        if (type is ParameterizedType) {
            type = type.actualTypeArguments[0]
            if (type is Class<*>) {
                return type
            }
        } else if (type is Class<*>) {
            return type
        }
        return null
    }

    fun getNestedGenericType(f: Field, genericTypeIndex: Int): Class<*>? {
        var type = f.genericType
        if (type is ParameterizedType) {
            type = type.actualTypeArguments[genericTypeIndex]
            return type as Class<*>
        }
        return if (type is Class<*>) {
            type
        } else null
    }

    fun isNumber(numberCls: Class<*>): Boolean {
        return numberCls == Long::class.javaPrimitiveType || numberCls == Long::class.java || numberCls == Int::class.javaPrimitiveType || numberCls == Int::class.java || numberCls == Short::class.javaPrimitiveType || numberCls == Short::class.java || numberCls == Byte::class.javaPrimitiveType || numberCls == Byte::class.java
    }

    private fun getPrimitiveDefaultValue(clazz: Class<*>): Any? {
        return if (clazz.isPrimitive) {
            if (clazz == Boolean::class.javaPrimitiveType) false else 0
        } else null
    }
}