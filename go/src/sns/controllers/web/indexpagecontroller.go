package web

type IndexPageController struct {
	BaseController
}

func (this *IndexPageController) WenjuanPage() {
	this.TplName = "wenjuan.html"
}
