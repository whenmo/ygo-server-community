package com.whenmo.service.cardinfo;

import com.whenmo.model.cardinfo.CardAttribute;
import com.whenmo.model.cardinfo.CardCategory;
import com.whenmo.model.cardinfo.CardInfoItem;
import com.whenmo.model.cardinfo.CardRace;
import com.whenmo.model.cardinfo.CardRule;
import com.whenmo.model.cardinfo.CardType;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CardInfoService {
    private final String STRINGS_PATH = "data/strings.conf";
    private List<CardInfoItem> cachedDefaultSetnames;
    private List<CardInfoItem> cachedDefaultCounters;
    private List<CardInfoItem> cachedDefaultVictorys;

    public List<CardInfoItem> getRules() {
        return Arrays.stream(CardRule.values()).map(r -> new CardInfoItem("rule", r.getCode(), r.getName())).toList();
    }

    public List<CardInfoItem> getAttributes() {
        return Arrays.stream(CardAttribute.values()).map(a -> new CardInfoItem("attribute", a.getCode(), a.getName()))
                .toList();
    }

    public List<CardInfoItem> getCategories() {
        return Arrays.stream(CardCategory.values()).map(c -> new CardInfoItem("category", c.getCode(), c.getName()))
                .toList();
    }

    public List<CardInfoItem> getRaces() {
        return Arrays.stream(CardRace.values()).map(r -> new CardInfoItem("race", r.getCode(), r.getName())).toList();
    }

    public List<CardInfoItem> getTypesMain() {
        return Arrays.stream(CardType.values()).filter(CardType::isTypeMain)
                .map(r -> new CardInfoItem("typeMain", r.getCode(), r.getName())).toList();
    }

    public List<CardInfoItem> getTypesMonster() {
        return Arrays.stream(CardType.values()).filter(CardType::isTypeMonster)
                .map(r -> new CardInfoItem("typeMonster", r.getCode(), r.getName())).toList();
    }

    public List<CardInfoItem> getTypesSpellTrap() {
        return Arrays.stream(CardType.values()).filter(CardType::isTypeSpellTrap)
                .map(r -> new CardInfoItem("typeSpellTrap", r.getCode(), r.getName())).toList();
    }

    public List<CardInfoItem> getDefaultSetnames() {
        if (cachedDefaultSetnames == null || cachedDefaultSetnames.isEmpty())
            resolveCacheDefaultLists();
        return cachedDefaultSetnames;
    }

    public List<CardInfoItem> getDefaultCounters() {
        if (cachedDefaultCounters == null || cachedDefaultCounters.isEmpty()) {
            resolveCacheDefaultLists();
        }
        return cachedDefaultCounters;
    }

    public List<CardInfoItem> getDefaultVictorys() {
        if (cachedDefaultVictorys == null || cachedDefaultVictorys.isEmpty()) {
            resolveCacheDefaultLists();
        }
        return cachedDefaultVictorys;
    }

    private void resolveCacheDefaultLists() {
        Path path = List.of(Path.of(STRINGS_PATH), Path.of("../" + STRINGS_PATH)).stream()
                .map(Path::toAbsolutePath).filter(Files::isRegularFile).findFirst()
                .orElseThrow(() -> new IllegalStateException("找不到 " + STRINGS_PATH));

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            List<CardInfoItem> setnames = new ArrayList<>();
            List<CardInfoItem> counters = new ArrayList<>();
            List<CardInfoItem> victorys = new ArrayList<>();

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                String[] parts = line.split("\\s+", 3);
                if (parts.length < 3)
                    continue;

                String type = parts[0];
                String codeHex = parts[1];

                if (!codeHex.matches("(?i)0x[0-9a-f]+"))
                    continue;

                try {
                    int code = Integer.parseInt(codeHex.substring(2), 16);
                    String name = parts[2].split("[\t\\s]+")[0];

                    if (type.equals("!setname")) {
                        setnames.add(new CardInfoItem("setname", code, name));
                    } else if (type.equals("!counter")) {
                        counters.add(new CardInfoItem("counter", code, name));
                    } else if (type.equals("!victory")) {
                        victorys.add(new CardInfoItem("victory", code, name));
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            cachedDefaultSetnames = setnames.stream().distinct().toList();
            cachedDefaultCounters = counters.stream().distinct().toList();
            cachedDefaultVictorys = victorys.stream().distinct().toList();

        } catch (IOException e) {
            throw new IllegalStateException("無法讀取 strings.conf 檔案", e);
        }
    }

    public synchronized void refreshCache() {
        cachedDefaultSetnames = null;
        cachedDefaultCounters = null;
        cachedDefaultVictorys = null;
        resolveCacheDefaultLists();
    }
}