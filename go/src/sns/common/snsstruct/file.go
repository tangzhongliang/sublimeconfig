package snsstruct

type FileGet struct {
	ErrDefine
	OK    bool   `json:"ok"`
	Files []File `json:"files"`
}

type File struct {
	Name string `json:"name"`
	Url  string `json:"url"`
}
