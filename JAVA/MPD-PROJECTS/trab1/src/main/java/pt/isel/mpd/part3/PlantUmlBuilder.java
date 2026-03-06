package pt.isel.mpd.part3;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;


public class PlantUmlBuilder {
    private final Writer writer;
    public Set<Class<?>> processesType = new HashSet<>();
    private Predicate<Member> memberFilter = member -> true;
    private Predicate<Class<?>> typeFilter = type -> true;
    private boolean flag = false;

    /**
     * Constrói uma instância que vai emitir os tipos via o writer
     * recebido na construção
     *
     * @param writer
     */
    public PlantUmlBuilder(Writer writer) {
        this.writer = writer;
    }

    /**
     * @param fileName
     */
    public PlantUmlBuilder(String fileName) {
        try {
            this.writer = new FileWriter(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adiciona, ao conjunto de tipos a processar, os tipos (classes ou interfaces), dos representantes
     * presentes no array "types", bem como as interfaces direta ou indiretamente implementadas e,
     * caso sejam classes, as suas superclasses.
     *
     * @param types
     */

    public PlantUmlBuilder addTypes(Class<?>... types) {
        if(types == null) { return this;}

        for (Class<?> type : types) {
            addTypeAndRelated(type);
        }
        return this;
    }

    private void addTypeAndRelated(Class<?> type) {
        if (type == null || type == Object.class) {
            return;
        }
        if (processesType.add(type)) {
            for (Class<?> iface : type.getInterfaces()) {
                addTypeAndRelated(iface);
            }
            addTypeAndRelated(type.getSuperclass());
        }
    }

    /**
     * Define um filtro (predicado) para seleccionar os membros a emitir
     * no ficheiro PlantUml
     *
     * @param filter
     */
    public PlantUmlBuilder memberFilter(Predicate<Member> filter) {
        this.memberFilter = filter;
        return this;
    }

    /**
     * Define um filtro (predicado) para seleccionar os tipos a emitir
     * no ficheiro PlantUml
     *
     * @param filter
     */
    public PlantUmlBuilder typeFilter(Predicate<Class<?>> filter) {
        this.typeFilter = filter;
        return this;
    }

    /**
     * Emite os tipos seleccionados e os correspondentes membros (campos, métodos e construtores),
     * seleccionados no
     * ficheiro (writer) PlantUml, sem considerar relações de herança, implementação
     * ou associações entre tipos através de campos
     */
    public PlantUmlBuilder emmitTypes() {
        for (Class<?> type : processesType) {
            if (typeFilter.test(type)) {
                emitType(type);
            }
        }
        return this;
    }

    private void emitType(Class<?> type) {
        try {
            if(type.isInterface()){
                writer.write("interface " + type.getSimpleName() + " {\n");
            } else {
                writer.write( (Modifier.isAbstract(type.getModifiers()) ? "abstract " : "")
                        + (Modifier.isStatic(type.getModifiers()) ? "static " : "")  + "class " + type.getSimpleName() + " {\n");
            }
            emitFields(type);
            emitConstructor(type);
            emitMethods(type);
            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void emitFields(Class<?> type) throws IOException {
        for (Field field : type.getDeclaredFields()) {
            if (memberFilter.test(field)) {
                writer.write("  " + getModifier(field.getModifiers()) + (Modifier.isStatic(field.getModifiers()) ? "{static} " : "")
                        + field.getName() + " : " + field.getType().getSimpleName() + "\n");
            }
        }
    }

    private void emitConstructor(Class<?> type) throws IOException {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (memberFilter.test(constructor)) {
                writer.write("  " + getModifier(constructor.getModifiers()) + constructor.getDeclaringClass().getSimpleName() + "(");
                writeParameters(constructor);
                writer.write(")\n");
            }
        }
    }

    private void emitMethods(Class<?> type) throws IOException {
        for (Method method : type.getDeclaredMethods()) {
            if (memberFilter.test(method)) {
                writer.write("  " + getModifier(method.getModifiers()) + (Modifier.isAbstract(method.getModifiers()) ? "{abstract} " : "")
                        + (Modifier.isStatic(method.getModifiers()) ? "{static} " : "") + method.getName() + "(");
                writeParameters(method);
                writer.write(") : " + method.getReturnType().getSimpleName() + "\n");
            }
        }
    }

    private void writeParameters(Executable executable) throws IOException {
        List<String> paramNames = getParamNames(executable);
        var params = executable.getParameters();
        for (int i = 0; i < params.length; i++) {
            String paramName = paramNames.size() > i ? paramNames.get(i) : params[i].getName();
            writer.write(paramName + " : " + params[i].getType().getSimpleName());
            if (i < params.length - 1) {
                writer.write(", ");
            }
        }
    }

    public static List<String> getParamNames(Executable executable) {
        ParametersNames annotation = executable.getAnnotation(ParametersNames.class);
        if (annotation != null) {
            String[] namesArray = annotation.value().split(";");
            List<String> names = new ArrayList<>();
            Collections.addAll(names, namesArray);
            return names;
        }
        return new ArrayList<>();
    }

    private String getModifier(int modifiers) {
        if (Modifier.isPublic(modifiers)) return "+";
        if (Modifier.isProtected(modifiers)) return "#";
        if (Modifier.isPrivate(modifiers)) return "-";
        return "";
    }

    /**
     * Emite associações de herança entre as classes e/ou interfaces usando a sintaxe PlantUml
     * Ex: BinExpr <|-- Add
     * para indicar que Add deriva de BinExpr
     * Ex: Iterable <|-- Collection para indicar que a interface Collection
     * deriva da interface Iterable
     */
    public PlantUmlBuilder emmitInheritanceAssociations() {
        for (Class<?> type : processesType){
            if(type != null && type.getSuperclass() != null &&
                    type != Object.class && type.getSuperclass() != Object.class) {
                if (typeFilter.test(type.getSuperclass())) {
                    try {
                        writer.write(type.getSuperclass().getSimpleName() + " <|-- " + type.getSimpleName() + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return this;
    }

    /**
     * Emite associações de implementação entre de interfaces, usando a sintaxe PlantUml
     * Ex: Expr <|.... BinExpr
     * para indicar que BinExpr implementa a interface
     */
    public PlantUmlBuilder emmitImplAssociations() {
        for (Class<?> type : processesType){
            if(type != null) {
                for (Class<?> iface : type.getInterfaces()) {
                    if (typeFilter.test(iface)) {
                        try {
                            writer.write(iface.getSimpleName() + " <|.. " + type.getSimpleName() + "\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * Emite associações entre classes através dos seus campos, usando a sintaxe PlantUml
     * Ex:  BinExpr "1" o----- "1" Expr : left
     * para indicar que BinExpr agrega um Expr através do seu campo left
     * A implementação deste método é opcional
     */

    public PlantUmlBuilder emmitFieldAssociations() {
        for (Class<?> type : processesType) {
            if (type != null) {
                for (Field field : ReflectionUtils.getAllFields(type)) {
                    if (memberFilter.test(field)) {
                        Class<?> fieldType = field.getType();
                        if (processesType.contains(fieldType) && !isFieldInherited(type, field)) {
                            try {
                                String cardinality = fieldType.isArray() || Collection.class.isAssignableFrom(fieldType) ? "*" : "1";
                                writer.write(type.getSimpleName() + " \"1\" o----- \"" + cardinality + "\" " +
                                        (fieldType.isArray() ? fieldType.getComponentType().getSimpleName() : fieldType.getSimpleName()) +
                                        " : " + field.getName() + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

    private boolean isFieldInherited(Class<?> type, Field field) {
        Class<?> superClass = type.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            try {
                superClass.getDeclaredField(field.getName());
                return true;
            } catch (NoSuchFieldException ignored) {
                superClass = superClass.getSuperclass();
            }
        }
        return false;
    }

    /**
     * Indicação o início da emissão no writer/ficheiro indicado na construçã
     * da instância PlantUmlBuilder
     * A partir desta chamada já não podem ser efectuadas chamadas para
     * adicionar novos tipos ou definir filtros de membro ou de tipo
     * @return
     */
    public PlantUmlBuilder start() {
        if (!flag) {
            try {
                writer.write("@startuml\n");
                writer.write("skinparam nodesep 20\n");
                writer.write("skinparam ranksep 20\n");
                writer.write("skinparam classAttributeIconSize 0\n");
                writer.write("hide empty members\n");
                writer.write("skinparam style strictuml\n");
                flag = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    /**
     * conclui a produção do ficheiro PlantUml
     */
    public void end() {
        if (flag) {
            try {
                writer.write("@enduml\n");
                writer.flush();
                writer.close();
                flag = false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}