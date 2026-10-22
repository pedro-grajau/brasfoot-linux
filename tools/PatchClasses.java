// SPDX-License-Identifier: GPL-3.0-only
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;

/** Small, version-bound bytecode hooks; never recompiles the original classes. */
public class PatchClasses implements Opcodes {
    static final String HOOK="mod/CompetitionMod";
    static int changes;
    static MethodInsnNode call(String name,String descriptor){
        return new MethodInsnNode(INVOKESTATIC,HOOK,name,descriptor,false);
    }
    static void returns(MethodNode method,int opcode,java.util.function.Supplier<InsnList> code){
        for(AbstractInsnNode instruction:method.instructions.toArray()){
            if(instruction.getOpcode()==opcode){method.instructions.insertBefore(instruction,code.get());changes++;}
        }
    }
    static InsnList invoke(String name){InsnList code=new InsnList();code.add(call(name,"()V"));return code;}
    public static void main(String[] args)throws Exception{
        Path output=Paths.get(args[1]);
        try(JarFile jar=new JarFile(args[0])){
            for(String className:new String[]{"best/f","best/a","best/at","a/al","a/de","a/di","f/G","best/I","a/hc","b/aC"}){
                byte[] bytes=jar.getInputStream(jar.getJarEntry(className+".class")).readAllBytes();
                ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
                int before=changes;
                for(MethodNode method:node.methods){
                    if(className.equals("b/aC")&&method.name.equals("getTableCellRendererComponent")&&method.desc.equals("(Ljavax/swing/JTable;Ljava/lang/Object;ZZII)Ljava/awt/Component;"))
                        returns(method,ARETURN,()->{InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,2));c.add(new VarInsnNode(ILOAD,6));c.add(call("rankingCell","(Ljava/awt/Component;Ljava/lang/Object;I)Ljava/awt/Component;"));return c;});
                    if(className.equals("best/I")){
                        if(method.name.equals("<init>")&&method.desc.equals("(Lbest/L;ILbest/ah;Lbest/ah;ILbest/at;Lbest/v;)V"))
                            returns(method,RETURN,()->{InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(call("prepareMatch","(Lbest/I;)V"));return c;});
                        if(method.name.equals("V")&&method.desc.equals("()V")){
                            InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(call("prepareMatch","(Lbest/I;)V"));method.instructions.insert(c);changes++;
                        }
                        if(method.name.equals("hf")&&method.desc.equals("()V"))
                            returns(method,RETURN,()->{InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(call("stadiumRevenue","(Lbest/I;)V"));return c;});
                    }
                    if(className.equals("a/hc")&&method.name.equals("rW")&&method.desc.equals("()V"))
                        returns(method,RETURN,()->{InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(call("rankingDetails","(Ljavax/swing/JPanel;)V"));return c;});
                    if(className.equals("f/G")&&method.name.equals("c")&&method.desc.equals("(IIZ)I"))
                        returns(method,IRETURN,()->{
                            InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));
                            c.add(call("nativePrize","(ILf/G;)I"));return c;
                        });
                    if(className.equals("best/f")){
                        if(method.name.equals("G")&&method.desc.equals("()V"))
                            returns(method,RETURN,()->invoke("loaded"));
                        if(method.name.equals("aR")&&method.desc.equals("()Lf/A;"))
                            returns(method,ARETURN,()->{InsnList c=new InsnList();c.add(call("annual","(Lf/A;)Lf/A;"));return c;});
                        if(method.name.equals("ah")&&method.desc.equals("()V")){
                            method.instructions.insert(invoke("seasonEnd"));changes++;
                        }
                        if(method.name.equals("ae")&&method.desc.equals("()V"))
                            returns(method,RETURN,()->invoke("seasonReady"));
                        if(method.name.equals("bB")&&method.desc.equals("()[Lbest/at;"))
                            returns(method,ARETURN,()->{InsnList c=new InsnList();c.add(call("allCompetitions","([Lbest/at;)[Lbest/at;"));return c;});
                    }
                    if(className.equals("best/a")&&method.name.equals("g")&&method.desc.equals("()V"))
                        returns(method,RETURN,()->invoke("calendar"));
                    if(className.equals("best/at")&&method.name.equals("a")&&method.desc.equals("(IZI)Ljava/util/ArrayList;"))
                        returns(method,ARETURN,()->{
                            InsnList c=new InsnList();c.add(new VarInsnNode(ILOAD,0));c.add(new VarInsnNode(ILOAD,1));
                            c.add(call("list","(Ljava/util/ArrayList;IZ)Ljava/util/ArrayList;"));return c;
                        });
                    if(className.equals("a/al")&&method.name.equals("u")&&method.desc.equals("(Lbest/at;)V")){
                        InsnList c=new InsnList();LabelNode original=new LabelNode();
                        c.add(new VarInsnNode(ALOAD,0));c.add(new VarInsnNode(ALOAD,1));
                        c.add(call("panel","(Ljavax/swing/JPanel;Lbest/at;)Z"));
                        c.add(new JumpInsnNode(IFEQ,original));c.add(new InsnNode(RETURN));c.add(original);
                        method.instructions.insert(c);changes++;
                    }
                    if(className.equals("a/de")&&method.name.equals("<init>")&&method.desc.equals("(Ljavax/swing/JDialog;Lbest/ah;)V"))
                        returns(method,RETURN,()->{
                            InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(new VarInsnNode(ALOAD,2));
                            c.add(call("trophyRoom","(Ljavax/swing/JPanel;Lbest/ah;)V"));return c;
                        });
                    if(className.equals("a/de")&&method.name.equals("pt")&&method.desc.equals("()[I"))
                        returns(method,ARETURN,()->{
                            InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(call("trophySize","([ILjavax/swing/JPanel;)[I"));return c;
                        });
                    if(className.equals("a/di")&&method.name.equals("<init>")&&method.desc.equals("(Ljavax/swing/JDialog;Lbest/ah;)V"))
                        returns(method,RETURN,()->{
                            InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(new VarInsnNode(ALOAD,2));
                            c.add(call("trophyRoom","(Ljavax/swing/JPanel;Lbest/ah;)V"));return c;
                        });
                    if(className.equals("a/di")&&method.name.equals("pG")&&method.desc.equals("()I"))
                        returns(method,IRETURN,()->{
                            InsnList c=new InsnList();c.add(new VarInsnNode(ALOAD,0));c.add(call("trophyCount","(ILjavax/swing/JPanel;)I"));return c;
                        });
                }
                if(changes==before)throw new IllegalStateException("Nenhum ponto de integração encontrado: "+className);
                ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS){
                    @Override protected String getCommonSuperClass(String left,String right){
                        try{
                            ClassLoader loader=PatchClasses.class.getClassLoader();
                            Class<?> a=Class.forName(left.replace('/','.'),false,loader);
                            Class<?> b=Class.forName(right.replace('/','.'),false,loader);
                            if(a.isAssignableFrom(b))return left;
                            if(b.isAssignableFrom(a))return right;
                            if(a.isInterface()||b.isInterface())return "java/lang/Object";
                            do{a=a.getSuperclass();}while(!a.isAssignableFrom(b));
                            return a.getName().replace('.','/');
                        }catch(ClassNotFoundException e){throw new IllegalStateException(left+" / "+right,e);}
                    }
                };
                node.accept(writer);
                Path target=output.resolve(className+".class");Files.createDirectories(target.getParent());Files.write(target,writer.toByteArray());
                System.out.println(className+": "+(changes-before)+" ponto(s)");
            }
        }
        if(changes!=18)throw new IllegalStateException("Esperados dezoito pontos de integração; encontrados "+changes);
    }
}
