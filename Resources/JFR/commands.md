## Steps
1. jcmd

2. jcmd <process_id> JFR.start name=<recording_name> filename=<output_filename>.jfr

3. jcmd <process_id> JFR.dump name=<recording_name>

4. jcmd <process_id> JFR.stop name=<recording_name>

5. kubectl cp <pod_name>:<filename> <local_filename>