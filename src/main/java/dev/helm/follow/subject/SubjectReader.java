package dev.helm.follow.subject;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;

import dev.helm.command.CommandException;
import dev.helm.command.chat.LineTokenizer;

public final class SubjectReader {

    private static final String NOT_A_MOB = "Not a mob: ";
    private static final String NOTHING_HERE = "Nothing here called ";

    private SubjectReader() {
    }

    public static FollowSubject read(String written, ClientLevel level) {
        List<String> tokens = LineTokenizer.tokenize(written == null ? "" : written);
        if (tokens.isEmpty()) {
            throw new CommandException("Expected at least one mob or player name.");
        }
        List<Identifier> mobs = new ArrayList<>();
        List<String> players = new ArrayList<>();
        for (String token : tokens) {
            if (!add(token, level, mobs, players)) {
                throw new CommandException(NOTHING_HERE + token + ".");
            }
        }
        return new FollowSubject(mobs, players);
    }

    private static boolean add(String token, ClientLevel level, List<Identifier> mobs,
                               List<String> players) {
        Identifier mob = MobTypes.identifying(token);
        if (mob != null) {
            mobs.add(mob);
            return true;
        }
        if (EntityTypeNames.identifierOf(token) != null) {
            throw new CommandException(NOT_A_MOB + token + ".");
        }
        if (PlayerRoster.holds(level, token)) {
            players.add(token);
            return true;
        }
        return false;
    }
}