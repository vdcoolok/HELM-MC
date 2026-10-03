package dev.helm.world.read.section;

public interface SectionMatcher {

    boolean accepts(int index);

    boolean fills();
}
