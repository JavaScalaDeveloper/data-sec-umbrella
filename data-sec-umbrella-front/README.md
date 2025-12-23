# 数据安全伞前端项目

这个项目通过 [Create React App](https://github.com/facebook/create-react-app) 引导创建。

## 可用脚本

在项目目录中，您可以运行：

### `npm start`

以开发模式运行应用程序。\
打开 [http://localhost:3000](http://localhost:3000) 在浏览器中查看它。

页面会在您进行更改时重新加载。\
您也可能在控制台中看到任何 lint 错误。

### `npm test`

在交互式监视模式下启动测试运行器。\
请参阅有关[运行测试](https://facebook.github.io/create-react-app/docs/running-tests)的章节以获取更多信息。

### `npm run build`

将应用构建到生产环境的 `build` 文件夹。\
它正确地捆绑了 React 并在生产模式下优化了构建以获得最佳性能。

构建被缩小，文件名包含哈希值。\
您的应用已准备好部署！

请参阅有关[部署](https://facebook.github.io/create-react-app/docs/deployment)的章节以获取更多信息。

### `npm run eject`

**注意：这是一个单向操作。一旦您 `eject`，就无法返回！**

如果您对构建工具和配置选择不满意，您可以随时 `eject`。

此命令将所有配置文件和传递依赖项（webpack、Babel、ESLint 等）复制到您的项目中，因此您可以完全控制它们。除了 `eject` 之外的所有命令仍然有效，但它们将指向复制的脚本，因此您可以调整它们。此时，您就靠自己了。

您不必永远使用 `eject`。精选的功能集适用于小型和中间部署，如果需要，您不应该觉得有义务使用此功能。但是，我们理解如果此工具在您准备好时无法自定义，它就不会有用。

## 项目结构

- `src/config/` - 配置文件，包括API配置
- `src/layouts/` - 布局组件
- `src/pages/` - 页面组件
  - `asset/` - 资产相关页面
  - `task/` - 任务相关页面，包括策略配置页面

## 启动项目

使用以下命令启动项目：

```bash
./start.sh
```

或者手动启动：

```bash
npm install
npm start
```