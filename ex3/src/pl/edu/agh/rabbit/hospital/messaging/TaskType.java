package pl.edu.agh.rabbit.hospital.messaging;

public enum TaskType {
    ELBOW,
    HIP,
    KNEE,
    UNKNOWN;

    public static TaskType parseType(String s) {
        switch (s.toUpperCase()) {
            case "ELBOW":
                return ELBOW;
            case "HIP":
                return HIP;
            case "KNEE":
                return KNEE;
            default:
                return UNKNOWN;
        }
    }
}
