"""
Automated Test Suite for RailBlock AI - ML Scoring Microservice
Tests health endpoint, model loading, and scoring inference across diverse edge cases.
"""

import requests
import json
import sys

BASE_URL = "http://localhost:8000"

def test_health():
    print("Testing GET /health ...", end=" ")
    try:
        r = requests.get(f"{BASE_URL}/health", timeout=5)
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        assert data.get("status") == "ok", "Status is not ok"
        assert data.get("model_loaded") is True, "Model is not loaded"
        print("PASSED ✅")
    except Exception as e:
        print(f"FAILED ❌: {e}")
        return False
    return True

def test_scoring_inference():
    print("Testing POST /score across scenarios ...")
    test_cases = [
        {
            "name": "High Urgency Critical Bridge (15d overdue)",
            "payload": {
                "severity": "Critical",
                "days_overdue": 15,
                "asset_type": "Bridge",
                "estimated_repair_hours": 8.0,
                "source_system": "TMS"
            },
            "min_expected_score": 25.0
        },
        {
            "name": "Medium Urgency Major Signal (5d overdue)",
            "payload": {
                "severity": "Major",
                "days_overdue": 5,
                "asset_type": "Signal",
                "estimated_repair_hours": 4.0,
                "source_system": "SMMS"
            },
            "min_expected_score": 8.0
        },
        {
            "name": "Low Urgency Minor Sleeper (Not overdue)",
            "payload": {
                "severity": "Minor",
                "days_overdue": -5,
                "asset_type": "Sleeper",
                "estimated_repair_hours": 2.0,
                "source_system": "TMS"
            },
            "max_expected_score": 5.0
        }
    ]

    all_passed = True
    for tc in test_cases:
        try:
            r = requests.post(f"{BASE_URL}/score", json=tc["payload"], timeout=5)
            assert r.status_code == 200, f"Expected 200, got {r.status_code}"
            data = r.json()
            score = data.get("score")
            mode = data.get("mode")
            assert mode == "ML", f"Expected mode 'ML', got {mode}"
            assert isinstance(score, (int, float)), "Score is not numeric"

            if "min_expected_score" in tc:
                assert score >= tc["min_expected_score"], f"Score {score} < {tc['min_expected_score']}"
            if "max_expected_score" in tc:
                assert score <= tc["max_expected_score"], f"Score {score} > {tc['max_expected_score']}"

            print(f"  [+] {tc['name']}: Score = {score:.2f} (Mode: {mode}) ✅")
        except Exception as e:
            print(f"  [-] {tc['name']}: FAILED ❌ - {e}")
            all_passed = False

    return all_passed

if __name__ == "__main__":
    print("=" * 60)
    print("      RUNNING ML SCORING MICROSERVICE TEST SUITE      ")
    print("=" * 60)
    h_ok = test_health()
    s_ok = test_scoring_inference()
    print("=" * 60)
    if h_ok and s_ok:
        print("ALL TESTS PASSED SUCCESSFULLY! 🚀")
        sys.exit(0)
    else:
        print("SOME TESTS FAILED! ❌")
        sys.exit(1)
