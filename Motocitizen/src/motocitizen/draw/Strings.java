package motocitizen.draw;

import motocitizen.accident.Accident;
import motocitizen.content.Medicine;
import motocitizen.utils.Const;

/**
 * Created by U_60A9 on 14.08.2015.
 */
public class Strings {
    public static String getAccidentTextToCopy(Accident accident) {
        StringBuilder res = new StringBuilder();
        res.append(Const.DATE_FORMAT.format(accident.getTime())).append(". ");
        res.append(accident.getType().toString()).append(". ");
        if (accident.getMedicine() != Medicine.UNKNOWN) {
            res.append(accident.getMedicine().toString()).append(". ");
        }
        res.append(accident.getAddress()).append(". ");
        res.append(accident.getDescription()).append(".");
        return res.toString();
    }

}