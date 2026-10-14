"""Проверка разметки уроков (для сопровождающих курса).

    python3 tools/validate_course.py

Проверяет: front matter с title; YAML-блоки quiz/task/tabs; номера ответов квизов;
существование модулей, файлов заданий и эталонных решений; вложенные ``` внутри блоков;
варианты ответов, «разрезанные» запятыми (несбалансированные скобки/кавычки).
Нужен PyYAML: pip install pyyaml
"""
import pathlib
import re
import sys

import yaml

root = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
errors = 0
lessons = 0


def err(msg):
    global errors
    print(msg)
    errors += 1


for md in sorted((root / "course").glob("*/*.md")):
    lessons += 1
    text = md.read_text(encoding="utf-8")
    rel = md.relative_to(root)
    if not text.startswith("---"):
        err(f"{rel}: нет front matter")
    else:
        fm = yaml.safe_load(text[3:text.index("\n---", 3)])
        if "title" not in fm:
            err(f"{rel}: нет title")
    for m in re.finditer(r"^```(quiz|task|tabs)\n(.*?)^```", text, re.S | re.M):
        kind, body = m.group(1), m.group(2)
        if re.search(r"^ {1,3}```", body, re.M):
            err(f"{rel}: {kind}: вложенный ``` внутри блока — используйте ~~~")
        try:
            data = yaml.safe_load(body)
        except Exception as e:
            err(f"{rel}: {kind}: ошибка YAML: {e}")
            continue
        if kind == "quiz":
            for i, q in enumerate(data):
                opts = q.get("options", [])
                for o in opts:
                    s = str(o)
                    if s.count("(") != s.count(")") or s.count("«") != s.count("»"):
                        err(f"{rel}: вариант ответа похож на разрезанный запятой: {s!r}")
                ans = q.get("answer")
                ans = ans if isinstance(ans, list) else [ans]
                if not q.get("q") or not opts or any(not isinstance(a, int) or a >= len(opts) for a in ans):
                    err(f"{rel}: квиз №{i + 1} некорректен: {q.get('q')}")
        elif kind == "task":
            for key in ("id", "title", "module", "test"):
                if key not in data:
                    err(f"{rel}: в задании нет {key}")
            if not (root / data.get("module", "") / "pom.xml").exists():
                err(f"{rel}: не найден модуль {data.get('module')}")
            for key in ("file", "solution"):
                if key in data and not (root / data[key]).exists():
                    err(f"{rel}: не найден файл {key}: {data[key]}")
        elif kind == "tabs" and not isinstance(data, dict):
            err(f"{rel}: tabs должен быть словарём «вкладка: markdown»")

# Данные браузерных тренажёров: games/*.yaml (первый вариант ответа — правильный)
games = 0
for gf in sorted((root / "portal/src/main/resources/games").glob("*.yaml")):
    games += 1
    rel = gf.relative_to(root)
    try:
        data = yaml.safe_load(gf.read_text(encoding="utf-8"))
    except Exception as e:
        err(f"{rel}: ошибка YAML: {e}")
        continue
    if gf.stem == "qa-marathon":
        items = [q for qs in data["topics"].values() for q in qs]
        bad = [q for q in items if not isinstance(q.get("q"), str) or not q.get("e")
               or len(q.get("o", [])) < 2 or not all(isinstance(o, str) for o in q["o"])]
    else:
        items = next(iter(data.values()))
        bad = [i for i in items if "options" in i and not all(isinstance(o, str) for o in i["options"])]
    for b in bad:
        err(f"{rel}: некорректный элемент (вариант с двоеточием без кавычек?): {str(b)[:80]}")

print(f"уроков: {lessons}, тренажёров: {games}, ошибок: {errors}")
sys.exit(1 if errors else 0)
