#!/usr/bin/env python3
"""
Writing assessment script for MinoLingo.
Called by Spring Boot via ProcessBuilder.
Usage: python writing_assess.py --text "..." --prompt "..." --level "BEGINNER"
Prints a single JSON object to stdout.
"""

import argparse
import json
import re
import sys
import requests

OLLAMA_URL = "https://minolingo.online/ollama/v1/completions"
MODEL = "qwen2.5:3b"


def check_grammar(text: str) -> dict:
    try:
        import language_tool_python
        tool = language_tool_python.LanguageTool("en-US")
        matches = tool.check(text)

        grammar_issues = []
        spelling_issues = []

        for match in matches:
            replacements = match.replacements[:3] if match.replacements else []
            word = text[match.offset: match.offset + match.errorLength]

            if match.ruleIssueType == "misspelling" or "SPELL" in (match.ruleId or ""):
                spelling_issues.append({
                    "word": word,
                    "suggestion": replacements[0] if replacements else "",
                    "offset": match.offset,
                    "length": match.errorLength
                })
            else:
                ctx_start = max(0, match.offset - 15)
                ctx_end = min(len(text), match.offset + match.errorLength + 15)
                grammar_issues.append({
                    "message": match.message,
                    "context": text[ctx_start:ctx_end],
                    "suggestion": replacements[0] if replacements else "",
                    "offset": match.offset,
                    "length": match.errorLength
                })

        total_words = max(len(text.split()), 1)
        grammar_score = max(0, int(100 - (len(grammar_issues) / total_words) * 300))
        spelling_score = max(0, int(100 - (len(spelling_issues) / total_words) * 400))

        return {
            "grammarIssues": grammar_issues,
            "spellingIssues": spelling_issues,
            "grammarScore": grammar_score,
            "spellingScore": spelling_score
        }

    except ImportError:
        # language_tool_python not installed — skip rule-based checks
        return {
            "grammarIssues": [],
            "spellingIssues": [],
            "grammarScore": 70,
            "spellingScore": 70
        }


def get_ai_feedback(text: str, prompt_title: str, level: str) -> dict:
    level_instruction = {
        "BEGINNER": "Be encouraging and gentle. Focus on the most important errors only. Use simple language.",
        "INTERMEDIATE": "Be supportive but thorough. Point out grammar patterns and suggest improvements.",
        "ADVANCED": "Be detailed and precise. Expect proper grammar, varied vocabulary, and coherent structure.",
    }.get(level, "Be supportive but thorough.")

    ai_prompt = (
        f'You are an English writing tutor for children on a learning platform.\n'
        f'A student was asked to write about: "{prompt_title}"\n'
        f'Their level is: {level}\n\n'
        f'{level_instruction}\n\n'
        f'Here is their writing:\n---\n{text}\n---\n\n'
        f'Return ONLY this JSON (no markdown, no extra text):\n'
        f'{{"contentScore":<0-100>,'
        f'"rephrasingSuggestions":['
        f'{{"original":"<exact phrase>","suggestion":"<better way>","reason":"<why>"}}],'
        f'"overallFeedback":"<2-3 encouraging sentences>"}}\n'
        f'Give 2-4 rephrasing suggestions. Start with {{ end with }}.'
    )

    try:
        response = requests.post(
            OLLAMA_URL,
            json={"model": MODEL, "prompt": ai_prompt, "max_tokens": 800},
            headers={"Content-Type": "application/json"},
            timeout=55
        )
        response.raise_for_status()
        data = response.json()

        raw = ""
        for choice in data.get("choices", []):
            if "text" in choice:
                raw = choice["text"].strip()
                break

        return _parse_ai_response(raw) if raw else _default_feedback()

    except Exception as e:
        print(f"[WARNING] Ollama call failed: {e}", file=sys.stderr)
        return _default_feedback()


def _parse_ai_response(raw: str) -> dict:
    raw = re.sub(r"```json\s*", "", raw)
    raw = re.sub(r"```\s*", "", raw).strip()

    start = raw.find("{")
    end = raw.rfind("}")
    if start < 0:
        return _default_feedback()
    raw = raw[start: end + 1] if end > start else raw[start:] + "}"
    raw = re.sub(r",\s*}", "}", raw)
    raw = re.sub(r",\s*]", "]", raw)

    try:
        parsed = json.loads(raw)
        return {
            "contentScore": int(parsed.get("contentScore", 50)),
            "rephrasingSuggestions": parsed.get("rephrasingSuggestions", []),
            "overallFeedback": parsed.get("overallFeedback", "Keep up the great work!")
        }
    except json.JSONDecodeError:
        return _default_feedback()


def _default_feedback() -> dict:
    return {
        "contentScore": 50,
        "rephrasingSuggestions": [],
        "overallFeedback": "Great effort! Keep practicing your writing skills."
    }


def calculate_score(grammar: int, spelling: int, content: int) -> int:
    return max(0, min(100, int(grammar * 0.35 + spelling * 0.25 + content * 0.40)))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--text", required=True)
    parser.add_argument("--prompt", required=True)
    parser.add_argument("--level", default="BEGINNER")
    args = parser.parse_args()

    grammar_result = check_grammar(args.text)
    ai_result = get_ai_feedback(args.text, args.prompt, args.level)

    overall = calculate_score(
        grammar_result["grammarScore"],
        grammar_result["spellingScore"],
        ai_result["contentScore"]
    )

    output = {
        "overallScore": overall,
        "grammarScore": grammar_result["grammarScore"],
        "spellingScore": grammar_result["spellingScore"],
        "contentScore": ai_result["contentScore"],
        "grammarIssues": grammar_result["grammarIssues"],
        "spellingIssues": grammar_result["spellingIssues"],
        "rephrasingSuggestions": ai_result["rephrasingSuggestions"],
        "overallFeedback": ai_result["overallFeedback"]
    }

    print(json.dumps(output, ensure_ascii=False))


if __name__ == "__main__":
    main()
