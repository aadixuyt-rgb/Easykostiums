# EasySnup 2.0 (Fabric 1.21.4) — kostium Snupa + kostium Adixa

Mod **klientowy** (widzisz tylko Ty). Zawiera też teksturki armoru (te same pliki są w modzie i w paczce `EasySnup-Textures.zip`).

## Jak zbudować (GitHub)
1. Utwórz repo na GitHubie i wrzuć całą zawartość tego folderu.
2. Wejdź w **Actions → Build EasySnup** (uruchomi się samo po pushu).
3. Na dole przebiegu pobierz artefakty: **EasySnup-mod** (jar) i **EasySnup-Textures** (zip z teksturami).

Lokalnie: `gradle build` (Java 21) → `build/libs/easysnup-2.0.0.jar`.

Jeśli build się wywali, skopiuj błąd z Actions i wyślij go do mnie, poprawię.

## Co jest w środku
- **Napis nad kostiumem na ziemi** – `Kostium` (szary) + `snupa` (błękitny) / `adixa` (czerwony).
- **Particles po założeniu** – kółka dookoła gracza (błękitne dla Snupa, czerwone dla Adixa).
- **GUI (klawisz B)** – przełącznik trybu **ZAMIANA / FAKE ITEM**, przyciski Snup/Adix (nic nie usuwają, tylko zamieniają/nadają) i **Usuń wszystko** (zamienione wracają do normy, nadane znikają, kostium jest zdejmowany).
- **Zamiana dotyczy tylko jednego itemu** (slotu w ręce) – inne takie same przedmioty w ekwipunku zostają zwykłe.
- **Kostium założony** → niewidzialny item nie da się wyrzucić (Q, Ctrl+Q, z ekwipunku, poza oknem), ale można go przesuwać. Fake item nigdy nie da się wyrzucić.
- **Ręka na F5** nie trzyma już niewidzialnego itemu.
- **/szafka** (/szafa) – armor stand → lista kostiumów: `[Coming soon][Adix][Snup][Coming soon][Coming soon]` (szare skórzane klaty z czerwoną nazwą).
- **2 kostiumy w ekwipunku** – weź drugi do ręki → zakłada się on, a pierwszy się ściąga.
- **Serca Snupa**: +2 fake serca regenerują się tylko przy pełnym prawdziwym życiu; utrata czerwonego serca kasuje wszystkie fake serca naraz; złote serca (koks/refil) ich nie ruszają.
- **Nazwy armoru**: po najechaniu na swoją zbroję przy założonym kostiumie – `Adixu armor` / `Armor snupika`.
- **Adix**: kolorowe hitboxy, po zabiciu gracza gra phonk (`sounds/phonk.ogg` z Twojego MP4, ~24 s) + 1,5 s czerwonych particles lecących we wszystkie strony z miejsca zgonu.

## Tekstury
Generowane skryptem `tools/gen_textures.py` (Pillow): błękitna klata Snupa (tył i ramiona błękitne, środek brzucha biały), spodnie/buty Snupa, oraz klata/spodnie/buty Adixa wycięte ze skina `Adixu_YT.png`. Głowa Adixa = kostka ze skina (`models/item/adix_head.json`).

## Zmiana nazwy "Coming soon"
`src/main/java/pl/easysnup/Szafka.java` → `comingSoon` / `lines("&cComing soon")`.
