#!/usr/bin/env python3
"""Builds app/src/main/assets/cities.tsv from GeoNames' cities15000 dump (CC BY 4.0, https://www.geonames.org).

Usage: python3 -I scripts/gen-cities.py <cities15000.txt> <admin1CodesASCII.txt> [output]

Input files come from https://download.geonames.org/export/dump/ (cities15000.zip and admin1CodesASCII.txt).
Output is one tab-separated row per city, largest first:
name, admin (first-level region, may be empty), countryCode, latitude, longitude, population, timezone
which is the format `CityList.parse` reads (see docs/architecture/18-flow-weather.md).
"""
import sys

COLUMNS = 19  # a GeoNames row has 19 tab-separated columns


def load_admin(path):
    names = {}
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            parts = line.rstrip("\n").split("\t")
            if len(parts) >= 2:
                names[parts[0]] = parts[1]
    return names


def main(argv):
    if len(argv) < 3:
        print(__doc__)
        return 1
    cities_path, admin_path = argv[1], argv[2]
    output = argv[3] if len(argv) > 3 else "app/src/main/assets/cities.tsv"
    admin = load_admin(admin_path)
    rows = []
    with open(cities_path, encoding="utf-8") as handle:
        for line in handle:
            parts = line.rstrip("\n").split("\t")
            if len(parts) < COLUMNS:
                continue
            name, latitude, longitude, country, region = parts[1], parts[4], parts[5], parts[8], parts[10]
            population, timezone = parts[14], parts[17]
            if not (name and country and timezone):
                continue
            rows.append((
                int(population or 0),
                [name, admin.get(f"{country}.{region}", ""), country, f"{float(latitude):.4f}", f"{float(longitude):.4f}", population or "0", timezone],
            ))
    rows.sort(key=lambda row: -row[0])
    with open(output, "w", encoding="utf-8", newline="\n") as handle:
        for _, fields in rows:
            handle.write("\t".join(field.replace("\t", " ") for field in fields) + "\n")
    print(f"wrote {output}: {len(rows)} cities")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
