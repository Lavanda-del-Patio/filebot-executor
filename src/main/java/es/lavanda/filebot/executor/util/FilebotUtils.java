package es.lavanda.filebot.executor.util;

import java.nio.file.Path;

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

    public String getFilebotCommandWithQuery(Path folderPath, String query) {
        return getFilebotCommand(folderPath) + " --q \"" + query + "\"";
    }

    public String getFilebotCommandWithNonStrict(Path folderPath) {
        return getFilebotCommand(folderPath) +
                FilebotConstants.NON_STRICT.toString();
    }

    public String getFilebotCommand(Path folderPath) {
        return FILEBOT + FilebotConstants.SCRIPT_AMC.toString() +
                getOutputFormat() +
                FilebotConstants.ACTION_TEST.toString() +
                FilebotConstants.LANG_ES.toString() +
                FilebotConstants.ORDER_AIRDATE.toString() +
                FilebotConstants.NO_XATTR.toString() +
                "\"" + folderPath + "\"" +
                FilebotConstants.DEF.toString() +
                getMoviesFormat() +
                getShowsFormat() +
                getStoreReport() +
                getUnsortedFormat() +
                getExcludeList();
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
        MOVIES_FORMAT(" 'movieFormat=/Peliculas/{n} ({y})' "),
        SHOWS_FORMAT(" 'seriesFormat=/Series/{n}/ Season {s}/{n} s{s.pad(2)}e{e.pad(2)}' "),
        UNSORTED_FORMAT(" 'unsortedFormat=/Unsorted/{fn}.{ext}' "),
        STORE_REPORT(" 'storeReport=FILEBOT_PATH_DATA/.reports' "),
        OUTPUT_FORMAT(" --output \"FILEBOT_PATH_OUTPUT\" "),
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
