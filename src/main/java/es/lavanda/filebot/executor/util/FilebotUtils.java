package es.lavanda.filebot.executor.util;

import java.nio.file.Path;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilebotUtils {
    private final String FILEBOT = "filebot";

    @Value("${filebot.path.input}")
    private String FILEBOT_PATH_INPUT;

    @Value("${filebot.path.output}")
    private String FILEBOT_PATH_OUTPUT;

    @Value("${filebot.path.data}")
    private String FILEBOT_PATH_DATA;

    @Value("${filebot.test.enabled}")
    private boolean FILEBOT_TEST_ENABLED;

    public String getRegisterCommand() {
        return FILEBOT + FilebotConstants.LICENSE.toString() +
                getFilebotPathData().trim() + "/license.psm";
    }

    public String getFilebotCommand(Path folderPath, String query, String label, boolean forceStrict) {
        String queryFilled = "";
        String nonStrict = "";
        String utLabel = "";
        if (Objects.nonNull(query)) {
            queryFilled = " --q \"" + query + "\" ";
        }
        if (forceStrict) {
            nonStrict = FilebotConstants.NON_STRICT.toString();
        }
        if (Objects.nonNull(label)) {
            utLabel = " --def \"ut_label=" + label.toLowerCase() + "\" ";
        }
        return FILEBOT + FilebotConstants.SCRIPT_AMC.toString() +
                getOutputFormat() +
                getAction() +
                FilebotConstants.LANG_ES.toString() +
                FilebotConstants.ORDER_AIRDATE.toString() +
                FilebotConstants.NO_XATTR.toString() +
                "\"" + folderPath + "\"" +
                FilebotConstants.DEF.toString() +
                getMoviesFormat() +
                getShowsFormat() +
                getStoreReport() +
                getUnsortedFormat() +
                getExcludeList() + queryFilled + nonStrict + utLabel;
    }

    private String getAction() {
        if (Boolean.TRUE.equals(FILEBOT_TEST_ENABLED)) {
            return FilebotConstants.ACTION_TEST.toString();
        } else
            return FilebotConstants.ACTION_MOVE.toString();
    }

    public String getFilebotPathInput() {
        return FILEBOT_PATH_INPUT;
    }

    public String getFilebotPathOutput() {
        return FILEBOT_PATH_OUTPUT;
    }

    public String getFilebotPathData() {
        return FILEBOT_PATH_DATA;
    }

    private enum Constants {
        MOVIES_FORMAT(" 'movieFormat=FILEBOT_PATH_OUTPUT/Peliculas/{n} ({y})' "),
        SHOWS_FORMAT(" 'seriesFormat=FILEBOT_PATH_OUTPUT/Series/{n}/ Season {s}/{n} s{s.pad(2)}e{e.pad(2)}' "),
        OUTPUT_FORMAT(" --output \"FILEBOT_PATH_OUTPUT\" "),
        UNSORTED_FORMAT(" 'unsortedFormat=/Unsorted/{fn}.{ext}' "),
        STORE_REPORT(" 'storeReport=FILEBOT_PATH_DATA/.reports' "),
        EXCLUDE_LIST(" 'excludeList=.excludes' ");

        private final String text;

        Constants(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private String getMoviesFormat() {
        return Constants.MOVIES_FORMAT.toString().replace("FILEBOT_PATH_OUTPUT", FILEBOT_PATH_OUTPUT);
    }

    private String getShowsFormat() {
        return Constants.SHOWS_FORMAT.toString().replace("FILEBOT_PATH_OUTPUT", FILEBOT_PATH_OUTPUT);
    }

    private String getUnsortedFormat() {
        return Constants.UNSORTED_FORMAT.toString().replace("FILEBOT_PATH_OUTPUT", FILEBOT_PATH_OUTPUT);
    }

    private String getStoreReport() {
        return Constants.STORE_REPORT.toString().replace("FILEBOT_PATH_DATA", FILEBOT_PATH_DATA);
    }

    private String getOutputFormat() {
        return Constants.OUTPUT_FORMAT.toString().replace("FILEBOT_PATH_OUTPUT", FILEBOT_PATH_OUTPUT);
    }

    private String getExcludeList() {
        return Constants.EXCLUDE_LIST.toString().replace("FILEBOT_PATH_DATA", FILEBOT_PATH_DATA);
    }
}
