package web

type PageController struct {
	UserCheckController
}

func (this *PageController) Prepare() {
	//	-----------------update session fail
	this.UserCheckController.Prepare()
	this.Layout = "user_layout.html"
	this.LayoutSections = make(map[string]string)
	this.LayoutSections["HtmlHead"] = ""
	this.LayoutSections["Scripts"] = ""
	this.LayoutSections["Sidebar"] = ""
}
