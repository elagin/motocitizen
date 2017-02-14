package motocitizen.user;

enum Role {
    RO("readonly", "только чтение"),
    BANNED("banned", "забанен"),
    STANDARD("standart", "пользователь"),
    MODERATOR("moderator", "модератор"),
    ADMINISTRATOR("admin", "администратор"),
    DEVELOPER("developer", "разработчик");

    private final String code;
    private final String text;

    Role(String code, String text) {
        this.code = code;
        this.text = text;
    }

    static Role parse(String role) {
        for (Role a : Role.values()) {
            if (a.code.equals(role)) return a;
        }
        return RO;
    }

    boolean isStandard()  {return this.compareTo(STANDARD) >= 0;}

    boolean isModerator() {return this.compareTo(MODERATOR) >= 0;}

    String getName()      {return this.text;}

    String getCode()      {return code;}
}
