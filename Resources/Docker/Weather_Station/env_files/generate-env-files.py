# Define the base template for a weather station deployment
base_template = """
STATION_ID={station_id}
LOCATION=
LATITUDE=
LONGITUDE=
"""

# Generate YAML files for stations 1 to 10
for station_id in range(1, 11):
    env_content = base_template.format(station_id=station_id)
    with open(f"env-{station_id}.list", "w") as env_file:
        env_file.write(env_content)
