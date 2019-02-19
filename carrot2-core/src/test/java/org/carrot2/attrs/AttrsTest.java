package org.carrot2.attrs;

import org.assertj.core.api.Assertions;
import org.carrot2.AbstractTest;
import org.junit.Test;

import java.util.Map;

public class AttrsTest extends AbstractTest {
  public interface Interface extends AcceptingVisitor {
  }

  public static class InterfaceImpl1 implements Interface {
    private AttrGroup group = new AttrGroup();

    public AttrInteger attrInt = group.register(
        "attrInt", AttrInteger.builder()
            .build());

    @Override
    public void accept(AttrVisitor visitor) {
      group.visit(visitor);
    }
  }

  public enum EnumClass {
    VALUE1,
    VALUE2,
    VALUE3;
  }

  @Test
  public void testExtractAndRestore() {

    class Component implements AcceptingVisitor {
      private AttrGroup attrs = new AttrGroup();

      public AttrBoolean attrBoolean = attrs.register(
          "attrBool", AttrBoolean.builder()
              .defaultValue(true)
              .build());

      public AttrBoolean attrBooleanNoValue = attrs.register(
          "attrBoolNoValue", AttrBoolean.builder()
              .build());

      public AttrInteger attrInt = attrs.register(
          "attrInt", AttrInteger.builder()
              .defaultValue(10)
              .build());

      public AttrInteger attrIntNoValue = attrs.register(
          "attrIntNoValue", AttrInteger.builder()
              .build());

      public AttrDouble attrDouble = attrs.register(
          "attrDouble", AttrDouble.builder()
              .defaultValue(36.6)
              .build());

      public AttrDouble attrDoubleNoValue = attrs.register(
          "attrDoubleNoValue", AttrDouble.builder()
              .build());

      public AttrString attrString = attrs.register(
          "attrString", AttrString.builder()
              .defaultValue("foo")
              .build());

      public AttrString attrStringNoValue = attrs.register(
          "attrStringNoValue", AttrString.builder()
              .build());

      public AttrObject<Interface> attrObject = attrs.register(
          "attrObject",
          AttrObject.builder(Interface.class)
              .defaultValue(new InterfaceImpl1())
              .build());

      public AttrEnum<EnumClass> attrEnum = attrs.register(
          "attrEnum",
          AttrEnum.builder(EnumClass.class)
              .defaultValue(EnumClass.VALUE1)
              .build());

      public AttrEnum<EnumClass> attrEnumNoValue = attrs.register(
          "attrEnumNoValue",
          AttrEnum.builder(EnumClass.class)
              .build());

      public final AttrStringArray attrStringArray =
          attrs.register("attrStringArray", AttrStringArray.builder()
              .defaultValue("foo", "bar", "baz")
              .build());

      public final AttrStringArray attrStringArrayNoValue =
          attrs.register("attrStringArrayNoValue", AttrStringArray.builder()
              .build());

      public InterfaceImpl1 attrConstantImpl = new InterfaceImpl1();
      public InterfaceImpl1 attrConstantImplNoValue;

      {
        attrs.register("attrConstantImpl",
            () -> attrConstantImpl,
            (val) -> attrConstantImpl = val,
            () -> new InterfaceImpl1());

        attrs.register("attrConstantImplNoValue",
            () -> attrConstantImplNoValue,
            (val) -> attrConstantImplNoValue = val,
            () -> new InterfaceImpl1());
      }

      @Override
      public void accept(AttrVisitor visitor) {
        attrs.visit(visitor);
      }
    }

    AliasMapper mapper = new AliasMapper();
    mapper.alias("component", Component.class, () -> new Component());
    mapper.alias("impl1", InterfaceImpl1.class, () -> new InterfaceImpl1());

    Component c1 = new Component();
    c1.attrInt.set(c1.attrInt.get() + 1);
    c1.attrDouble.set(Math.PI);
    c1.attrObject.set(new InterfaceImpl1(),
        (impl) -> impl.attrInt.set(42));
    c1.attrEnum.set(EnumClass.VALUE2);

    c1.attrConstantImpl.attrInt.set(42);

    Component c2 = restore(Component.class, extract(c1, mapper), mapper);
    Assertions.assertThat(c2.attrInt.get()).isEqualTo(c1.attrInt.get());

    System.out.println(Attrs.toPrettyString(c1));
  }

  public static <E extends AcceptingVisitor> E restore(Class<? extends E> clazz,
                                                       Map<String, Object> attrs,
                                                       ClassNameMapper mapper) {
    return Attrs.fromMap(clazz, attrs, mapper::fromName);
  }

  private static Map<String, Object> extract(AcceptingVisitor ob, ClassNameMapper mapper) {
    return Attrs.toMap(ob, mapper::toName);
  }
}
