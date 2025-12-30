# ディレクトリ構成（directory structure）

```bash
media-gallery-generator-cli
├─ README.md
├─ pom.xml
├─ .gitignore
├─ gallery.cmd
│
├─ docs/
│   ├─ design.md
│   ├─ directory-structure.md
│   └─ usage.md
│
├─ .vscode/
│   ├─ launch.json
│   └─ tasks.json
│
├─ work/                            # 作業用ディレクトリ
│   ├─ gallery.config.example.json
│   ├─ gallery.config.json          # initで生成されるファイル。Git管理外
│   ├─ media/
│   │   ├─ sample-01.jpg
│   │   └─ sample-02.jpg
│   └─ dist/                        # buildで生成される。Git管理外
│       ├─ .gitkeep
│       └─ assets/
│           └─ .gitkeep
│
└─ src/
    ├─ main/
    │   ├─ java/
    │   │   └─ com/
    │   │       └─ example/
    │   │           └─ gallery/
    │   │               ├─ Main.java
    │   │               │
    │   │               ├─ cli/
    │   │               │   ├─ GalleryCommand.java
    │   │               │   ├─ InitCommand.java
    │   │               │   └─ BuildCommand.java
    │   │               │
    │   │               ├─ config/
    │   │               │   ├─ AppConfig.java
    │   │               │   ├─ ConfigLoader.java
    │   │               │   ├─ ConfigPaths.java
    │   │               │   ├─ ConfigValidationException.java
    │   │               │   └─ DefaultConfigWriter.java
    │   │               │
    │   │               ├─ domain/
    │   │               │   ├─ MediaItem.java
    │   │               │   ├─ MediaType.java
    │   │               │   └─ SortMode.java
    │   │               │
    │   │               ├─ scan/
    │   │               │   ├─ MediaScanner.java
    │   │               │   ├─ MediaFilter.java
    │   │               │   ├─ MediaMetadataReader.java
    │   │               │   └─ MediaScanException.java
    │   │               │
    │   │               ├─ output/
    │   │               │   ├─ OutputPreparer.java
    │   │               │   ├─ SafePaths.java
    │   │               │   ├─ OutputPaths.java
    │   │               │   ├─ OutputPreparationException.java
    │   │               │   ├─ AssetCopier.java
    │   │               │   └─ AssetCopyException.java
    │   │               │
    │   │               └─ render/
    │   │                   ├─ HtmlGalleryRenderer.java
    │   │                   ├─ HtmlTemplateLoader.java
    │   │                   └─ HtmlWriteException.java
    │   │
    │   └─ resources/
    │       ├─ templates/
    │       │   └─ index.html
    │       └─ default-config/
    │           └─ gallery.config.json
    │
    └─ test/
        └─ java/
            └─ com/
                └─ example/
                    └─ gallery/
                        ├─ SmokeTest.java
                        ├─ config/
                        │   └─ ConfigLoaderTest.java
                        ├─ scan/
                        │   └─ MediaScannerTest.java
                        ├─ output/
                        │   ├─ OutputPreparerTest.java
                        │   └─ AssetCopierTest.java
                        └─ render/
                            └─ HtmlGalleryRendererTest.java
```
