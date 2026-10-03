package dev.helm.follow.subject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public record FollowSubject(List<Identifier> mobs, List<String> players) {

    public FollowSubject {
        mobs = List.copyOf(mobs);
        players = List.copyOf(players);
    }

    public boolean matches(Entity entity) {
        if (entity == null) {
            return false;
        }
        return mobs.contains(EntityTypeNames.identifierOf(entity))
                || called(entity.getName().getString());
    }

    private boolean called(String candidate) {
        for (String name : players) {
            if (name.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

    public String describe() {
        return listed();
    }

    public String announce() {
        return "Following " + listed() + ".";
    }

    public String listed() {
        List<String> all = new ArrayList<>(
                mobs.stream().map(Identifier::toString).collect(Collectors.toList()));
        all.addAll(players);
        return String.join(", ", all);
    }

    @Override
    public String toString() {
        return listed();
    }
}