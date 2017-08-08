package cmmodels

type FileGet struct {
	OK      bool   `json:"ok"`
	Err     string `json:"err"`
	Type    string `json:"type"`
	Title   string `json:"title"`
	TeamID  string `json:"team_id"`
	UserID  string `json:"user_id"`
	Channel string `json:"channel"`
	Files   []File `json:"files"`
}

type File struct {
	Name string `json:"name"`
	Url  string `json:"url"`
}

const (
	COMMAND_TYPE_PRINT        = "COMMAND_TYPE_PRINT"
	COMMAND_TYPE_PJS_SHOW     = "COMMAND_TYPE_PJS_SHOW"
	COMMAND_TYPE_PJS_UPLOAD   = "COMMAND_TYPE_PJS_UPLOAD"
	COMMAND_TYPE_IWB_SHOW     = "COMMAND_TYPE_IWB_SHOW"
	COMMAND_TYPE_IWB_UPLOAD   = "COMMAND_TYPE_IWB_UPLOAD"
	COMMAND_TYPE_NONE         = "COMMAND_TYPE_NONE"
	LOCAL_SERVER_MESSAGE      = "LOCAL_SERVER_MESSAGE"
	COMMAND_TYPE_LANGUAGE     = "COMMAND_TYPE_LANGUAGE"
	COMMAND_TYPE_HELP         = "COMMAND_TYPE_HELP"
	COMMAND_TYPE_IWB_START    = "COMMAND_TYPE_IWB_START"
	COMMAND_TYPE_IWB_SHUTDOWN = "COMMAND_TYPE_IWB_SHUTDOWN"
	COMMAND_TYPE_PJS_START    = "COMMAND_TYPE_PJS_START"
	EVENT_JOIN
	EVENT_LEAVE
	EVENT_MESSAGE
)

type Event struct {
	Type          string `json:"type"`
	TeamID        string `json:"team_id"`
	Channel       string `json:"channel"`
	Timestamp     int    `json:"time"`
	Content       string `json:"data"`
	MessageType   int    `json:"data_type"`
	IP            string `json:"ip"`
	LocalServerId string
	MessageTS     string `json:"ts"`
	UserID        string `json:"user_id"`
	DeviceId      string `json:device_id`
}
