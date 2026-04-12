import csv
import json
import uuid
import time
import argparse
import sys

def generate_node(title, description, node_type="IDEA", x=0.0, y=0.0):
    return {
        "id": str(uuid.uuid4()),
        "type": node_type.upper(),
        "title": title,
        "description": description,
        "data": "{}",
        "createdAt": int(time.time() * 1000),
        "updatedAt": int(time.time() * 1000),
        "x": float(x),
        "y": float(y),
        "isLocked": False
    }

def convert_csv_to_omnimap_json(input_csv, output_json):
    nodes = []
    edges = []

    try:
        with open(input_csv, mode='r', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            
            # Simple automatic layout
            current_y = 0.0
            
            for row in reader:
                # Fallback to sensible defaults if columns are missing
                title = row.get('title', 'Untitled')
                description = row.get('description', '')
                node_type = row.get('type', 'IDEA')
                x = row.get('x', 100.0)
                y = row.get('y', current_y)
                
                nodes.append(generate_node(title, description, node_type, x, y))
                
                # Cascade them downwards
                current_y += 150.0

        # Construct the final OmniMap payload
        omnimap_payload = {
            "nodes": nodes,
            "edges": edges # Edges could be generated here if CSV supported parent/child relationships
        }

        with open(output_json, mode='w', encoding='utf-8') as jsonfile:
            json.dump(omnimap_payload, jsonfile, indent=4)
            
        print(f"Successfully converted {len(nodes)} nodes from {input_csv} to {output_json}")

    except Exception as e:
        print(f"Error during conversion: {e}", file=sys.stderr)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="OmniMap CSV to JSON Importer")
    parser.add_argument("input", help="Path to the input CSV file")
    parser.add_argument("output", help="Path to save the output JSON file", nargs="?", default="omnimap_backup.json")
    
    args = parser.parse_args()
    convert_csv_to_omnimap_json(args.input, args.output)
