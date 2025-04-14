import pdfplumber
import json
import re


CHECKMARK_COORDS = {
    "Care of Babies": {"x0": 235, "y0": 511, "x1": 243, "y1": 519, "width": 8, "height": 8},
    "Care of Toddler": {"x0": 235, "y0": 492, "x1": 243, "y1": 500, "width": 8, "height": 8},
    "Care of Children": {"x0": 235, "y0": 474, "x1": 243, "y1": 482, "width": 8, "height": 8},
    "Care of Elderly": {"x0": 235, "y0": 455, "x1": 243, "y1": 463, "width": 8, "height": 8},
    "Care of Disabled": {"x0": 235, "y0": 437, "x1": 243, "y1": 444, "width": 8, "height": 8},
    "Care of Bedridden": {"x0": 235, "y0": 419, "x1": 243, "y1": 425, "width": 8, "height": 8},
    "Care of Pet": {"x0": 235, "y0": 401, "x1": 243, "y1": 409, "width": 8, "height": 8},
    "Household Works": {"x0": 235, "y0": 382, "x1": 243, "y1": 390, "width": 8, "height": 8},
    "Car Washing": {"x0": 235, "y0": 362, "x1": 243, "y1": 370, "width": 8, "height": 8},
    "Gardening": {"x0": 235, "y0": 343, "x1": 243, "y1": 351, "width": 8, "height": 8},
    "Cooking": {"x0": 235, "y0": 326, "x1": 243, "y1": 334, "width": 8, "height": 8},
    "Driving": {"x0": 235, "y0": 308, "x1": 243, "y1": 315, "width": 8, "height": 8},
    "Mandarin (Poor)": {"x0": 145, "y0": 87, "x1": 153, "y1": 95, "width": 8, "height": 8},
    "Cantonese (Poor)": {"x0": 145, "y0": 69, "x1": 153, "y1": 77, "width": 8, "height": 8},
    "English (Poor)": {"x0": 145, "y0": 49, "x1": 153, "y1": 57, "width": 8, "height": 8},
    "Mandarin (Good)": {"x0": 250, "y0": 87, "x1": 258, "y1": 95, "width": 8, "height": 8},
    "Cantonese (Good)": {"x0": 250, "y0": 69, "x1": 258, "y1": 77, "width": 8, "height": 8},
    "English (Good)": {"x0": 250, "y0": 49, "x1": 258, "y1": 57, "width": 8, "height": 8},
}

def is_matching_coords(image, coords):
    """

    """
    return (
            image["x0"] == coords["x0"] and
            image["y0"] == coords["y0"] and
            image["x1"] == coords["x1"] and
            image["y1"] == coords["y1"]
    )

def remove_null_and_empty_strings(data):
    """

    """
    if isinstance(data, list):
        return [remove_null_and_empty_strings(item) for item in data if item not in (None, "")]
    elif isinstance(data, dict):
        return {key: remove_null_and_empty_strings(value) for key, value in data.items() if value not in (None, "")}
    else:
        return data

def remove_chinese_characters(text):
    """

    """
    if isinstance(text, str):

        return re.sub(r'[\u4e00-\u9fff]', '', text)
    return text

def process_text_remove_chinese(data):
    """

    """
    if isinstance(data, dict):
        return {key: process_text_remove_chinese(value) for key, value in data.items()}
    elif isinstance(data, list):
        return [process_text_remove_chinese(item) for item in data]
    elif isinstance(data, str):
        return remove_chinese_characters(data)
    return data

def extract_pdf_to_json_with_checkmarks(pdf_path):
    """

    """
    data = {"pages": []}


    with pdfplumber.open(pdf_path) as pdf:
        page = pdf.pages[0]
        page_data = {
            "page_number": 1,
            "tables": [],
            "image_checkmarks": [],
            "language_abilities": {},
            "contains_checkmark": False,
            "text": page.extract_text() if page.extract_text() else ""
        }


        tables = page.extract_tables()
        for table in tables:
            processed_table = []
            for row in table:
                processed_row = []
                for cell in row:
                    if cell is not None and cell in CHECKMARK_COORDS:
                        coords = CHECKMARK_COORDS[cell]
                        contains_checkmark = any(
                            is_matching_coords(image, coords) for image in page.images
                        )
                        processed_row.append(cell)
                        processed_row.append("true" if contains_checkmark else "false")
                    else:
                        processed_row.append(cell)
                        processed_row.append("")
                processed_table.append(processed_row)
            page_data["tables"].append(processed_table)

        language_abilities = {}
        for key, coords in CHECKMARK_COORDS.items():
            for image in page.images:
                if is_matching_coords(image, coords):
                    if " (" in key:
                        ability, level = key.split(" (")
                        level = level.strip(")")
                        language_abilities[ability] = level

        page_data["language_abilities"] = language_abilities
        page_data["contains_checkmark"] = len(language_abilities) > 0

        data["pages"].append(page_data)

    data = remove_null_and_empty_strings(data)

    data = process_text_remove_chinese(data)

    return json.dumps(data, ensure_ascii=False, indent=4)