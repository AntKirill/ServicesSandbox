package applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.business;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

class CardDescriptionHandler {
    private final @NotNull String myBeginningDescription;
    private final @NotNull Pattern htmlPattern;

    public CardDescriptionHandler(@Nullable String description) {
        if (description == null) {
            myBeginningDescription = "";
        } else {
            myBeginningDescription = description;
        }
        htmlPattern = Pattern.compile(".*<[^>]+>.*", Pattern.DOTALL);
    }

    protected boolean isHtmlDescription() {
        return htmlPattern.matcher(myBeginningDescription).matches();
    }

    protected String processHtmlDescription() {
        final MutableDataSet dataHolder = new MutableDataSet();
        dataHolder.set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false);
        final FlexmarkHtmlConverter renderer = FlexmarkHtmlConverter.builder(dataHolder).build();
        return renderer.convert(myBeginningDescription);
    }

    public String getDescriptionPresentation() {
        if (!isHtmlDescription()) {
            return myBeginningDescription;
        }
        return processHtmlDescription();
    }
}
