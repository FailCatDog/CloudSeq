# Domain package migration - moves biz files and rewrites packages/imports
$ErrorActionPreference = "Stop"
$bizRoot = "E:\Code-Project\Project\CloudSeq\backend\src\main\java\cn\guet\soft_manage\biz"
$javaRoot = "E:\Code-Project\Project\CloudSeq\backend\src\main\java"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

$moves = @(
  # user
  @("controller\UserController.java", "user\controller\UserController.java"),
  @("service\UserService.java", "user\service\UserService.java"),
  @("service\impl\UserServiceImpl.java", "user\service\impl\UserServiceImpl.java"),
  @("dao\UserDao.java", "user\dao\UserDao.java"),
  @("pojo\entity\User.java", "user\entity\User.java"),
  @("pojo\param\UserParam.java", "user\param\UserParam.java"),
  @("pojo\dto\user\LoginRequestDTO.java", "user\dto\LoginRequestDTO.java"),
  @("pojo\dto\user\LoginResponseDTO.java", "user\dto\LoginResponseDTO.java"),
  @("pojo\dto\user\RegisterRequestDTO.java", "user\dto\RegisterRequestDTO.java"),
  # team
  @("controller\TeamController.java", "team\controller\TeamController.java"),
  @("service\TeamService.java", "team\service\TeamService.java"),
  @("service\impl\TeamServiceImpl.java", "team\service\impl\TeamServiceImpl.java"),
  @("dao\TeamDao.java", "team\dao\TeamDao.java"),
  @("dao\TeamMemberDao.java", "team\dao\TeamMemberDao.java"),
  @("dao\TopicApprovalDao.java", "team\dao\TopicApprovalDao.java"),
  @("pojo\entity\Team.java", "team\entity\Team.java"),
  @("pojo\entity\TeamMember.java", "team\entity\TeamMember.java"),
  @("pojo\entity\TopicApproval.java", "team\entity\TopicApproval.java"),
  @("pojo\dto\team\TeamCreateRequestDTO.java", "team\dto\TeamCreateRequestDTO.java"),
  @("pojo\dto\team\TeamMemberAddRequestDTO.java", "team\dto\TeamMemberAddRequestDTO.java"),
  @("pojo\dto\team\TeamMemberInfoDTO.java", "team\dto\TeamMemberInfoDTO.java"),
  @("pojo\dto\team\TeamMembersResponseDTO.java", "team\dto\TeamMembersResponseDTO.java"),
  @("pojo\dto\team\TeamTopicSubmitRequestDTO.java", "team\dto\TeamTopicSubmitRequestDTO.java"),
  @("pojo\dto\team\TopicApprovalReviewRequestDTO.java", "team\dto\TopicApprovalReviewRequestDTO.java"),
  # workspace
  @("controller\WorkspaceController.java", "workspace\controller\WorkspaceController.java"),
  @("controller\WorkspaceNodeController.java", "workspace\controller\WorkspaceNodeController.java"),
  @("controller\PlanTaskController.java", "workspace\controller\PlanTaskController.java"),
  @("controller\WeeklyReportController.java", "workspace\controller\WeeklyReportController.java"),
  @("service\WorkspaceService.java", "workspace\service\WorkspaceService.java"),
  @("service\WorkspaceNodeService.java", "workspace\service\WorkspaceNodeService.java"),
  @("service\WorkspaceAccessService.java", "workspace\service\WorkspaceAccessService.java"),
  @("service\PlanTaskService.java", "workspace\service\PlanTaskService.java"),
  @("service\WeeklyReportService.java", "workspace\service\WeeklyReportService.java"),
  @("service\impl\WorkspaceServiceImpl.java", "workspace\service\impl\WorkspaceServiceImpl.java"),
  @("service\impl\WorkspaceNodeServiceImpl.java", "workspace\service\impl\WorkspaceNodeServiceImpl.java"),
  @("service\impl\WorkspaceAccessServiceImpl.java", "workspace\service\impl\WorkspaceAccessServiceImpl.java"),
  @("service\impl\PlanTaskServiceImpl.java", "workspace\service\impl\PlanTaskServiceImpl.java"),
  @("service\impl\WeeklyReportServiceImpl.java", "workspace\service\impl\WeeklyReportServiceImpl.java"),
  @("dao\WorkspaceDao.java", "workspace\dao\WorkspaceDao.java"),
  @("dao\WorkspaceNodeDao.java", "workspace\dao\WorkspaceNodeDao.java"),
  @("dao\WorkspaceContentDao.java", "workspace\dao\WorkspaceContentDao.java"),
  @("dao\WorkspaceLockDao.java", "workspace\dao\WorkspaceLockDao.java"),
  @("dao\PlanTaskDao.java", "workspace\dao\PlanTaskDao.java"),
  @("dao\WeeklyReportDao.java", "workspace\dao\WeeklyReportDao.java"),
  @("pojo\entity\Workspace.java", "workspace\entity\Workspace.java"),
  @("pojo\entity\WorkspaceNode.java", "workspace\entity\WorkspaceNode.java"),
  @("pojo\entity\WorkspaceContent.java", "workspace\entity\WorkspaceContent.java"),
  @("pojo\entity\WorkspaceLock.java", "workspace\entity\WorkspaceLock.java"),
  @("pojo\entity\PlanTask.java", "workspace\entity\PlanTask.java"),
  @("pojo\entity\WeeklyReport.java", "workspace\entity\WeeklyReport.java"),
  @("pojo\dto\workspace\WorkspaceAccessContext.java", "workspace\dto\WorkspaceAccessContext.java"),
  @("pojo\dto\workspace\WorkspaceNodeCreateRequestDTO.java", "workspace\dto\WorkspaceNodeCreateRequestDTO.java"),
  @("pojo\dto\workspace\WorkspaceNodeRenameRequestDTO.java", "workspace\dto\WorkspaceNodeRenameRequestDTO.java"),
  @("pojo\dto\workspace\WorkspaceNodeTreeDTO.java", "workspace\dto\WorkspaceNodeTreeDTO.java"),
  @("utils\IsoWeekUtil.java", "workspace\util\IsoWeekUtil.java"),
  # document
  @("controller\DocumentController.java", "document\controller\DocumentController.java"),
  @("controller\DocumentCommentController.java", "document\controller\DocumentCommentController.java"),
  @("controller\DocumentAssetController.java", "document\controller\DocumentAssetController.java"),
  @("controller\AssetProxyController.java", "document\controller\AssetProxyController.java"),
  @("controller\CollabInternalController.java", "document\controller\CollabInternalController.java"),
  @("service\DocumentService.java", "document\service\DocumentService.java"),
  @("service\DocumentCommentService.java", "document\service\DocumentCommentService.java"),
  @("service\DocumentAssetService.java", "document\service\DocumentAssetService.java"),
  @("service\impl\DocumentServiceImpl.java", "document\service\impl\DocumentServiceImpl.java"),
  @("service\impl\DocumentCommentServiceImpl.java", "document\service\impl\DocumentCommentServiceImpl.java"),
  @("service\impl\DocumentAssetServiceImpl.java", "document\service\impl\DocumentAssetServiceImpl.java"),
  @("dao\DocumentCommentDao.java", "document\dao\DocumentCommentDao.java"),
  @("dao\WorkspaceAssetDao.java", "document\dao\WorkspaceAssetDao.java"),
  @("pojo\entity\DocumentComment.java", "document\entity\DocumentComment.java"),
  @("pojo\entity\WorkspaceAsset.java", "document\entity\WorkspaceAsset.java"),
  @("pojo\dto\document\DocumentAssetReadDTO.java", "document\dto\DocumentAssetReadDTO.java"),
  @("pojo\dto\document\DocumentAssetUploadResponseDTO.java", "document\dto\DocumentAssetUploadResponseDTO.java"),
  @("pojo\dto\document\DocumentCommentCreateRequestDTO.java", "document\dto\DocumentCommentCreateRequestDTO.java"),
  @("pojo\dto\document\DocumentCommentDTO.java", "document\dto\DocumentCommentDTO.java"),
  @("pojo\dto\document\DocumentContentSaveRequestDTO.java", "document\dto\DocumentContentSaveRequestDTO.java"),
  @("pojo\dto\document\DocumentDetailDTO.java", "document\dto\DocumentDetailDTO.java"),
  @("pojo\dto\collab\CollabLoadResponseDTO.java", "document\dto\CollabLoadResponseDTO.java"),
  @("pojo\dto\collab\CollabPersistRequestDTO.java", "document\dto\CollabPersistRequestDTO.java"),
  @("pojo\dto\collab\CollabPersistResponseDTO.java", "document\dto\CollabPersistResponseDTO.java"),
  @("pojo\dto\collab\CollabTokenClaimsDTO.java", "document\dto\CollabTokenClaimsDTO.java"),
  @("pojo\dto\collab\CollabTokenResponseDTO.java", "document\dto\CollabTokenResponseDTO.java"),
  @("utils\CollabTokenUtil.java", "document\util\CollabTokenUtil.java"),
  @("utils\DocumentAssetUrlHelper.java", "document\util\DocumentAssetUrlHelper.java"),
  @("utils\DocumentAssetValidator.java", "document\util\DocumentAssetValidator.java"),
  @("utils\WorkspaceContentMetadataUtil.java", "document\util\WorkspaceContentMetadataUtil.java"),
  # dict
  @("controller\DictController.java", "dict\controller\DictController.java"),
  @("service\DictService.java", "dict\service\DictService.java"),
  @("service\impl\DictServiceImpl.java", "dict\service\impl\DictServiceImpl.java"),
  @("dao\DictKeyDao.java", "dict\dao\DictKeyDao.java"),
  @("dao\DictValueDao.java", "dict\dao\DictValueDao.java"),
  @("pojo\entity\DictKey.java", "dict\entity\DictKey.java"),
  @("pojo\entity\DictValue.java", "dict\entity\DictValue.java"),
  @("pojo\dto\dict\DictValueDTO.java", "dict\dto\DictValueDTO.java"),
  # common
  @("utils\RedissonUtil.java", "common\util\RedissonUtil.java")
)

foreach ($pair in $moves) {
  $src = Join-Path $bizRoot $pair[0]
  $dest = Join-Path $bizRoot $pair[1]
  $destDir = Split-Path $dest -Parent
  if (-not (Test-Path $src)) { throw "Missing source: $src" }
  New-Item -ItemType Directory -Force -Path $destDir | Out-Null
  Move-Item -Path $src -Destination $dest -Force
  $rel = $pair[1] -replace '\\', '/' -replace '/[^/]+\.java$', '' -replace '/', '.'
  $pkg = "cn.guet.soft_manage.biz.$rel"
  $content = [System.IO.File]::ReadAllText($dest, [System.Text.Encoding]::UTF8)
  $content = $content -replace '^package\s+[^;]+;', "package $pkg;"
  [System.IO.File]::WriteAllText($dest, $content, $utf8NoBom)
}

$replacements = [ordered]@{
  'cn.guet.soft_manage.biz.pojo.dto.collab.' = 'cn.guet.soft_manage.biz.document.dto.'
  'cn.guet.soft_manage.biz.pojo.dto.document.' = 'cn.guet.soft_manage.biz.document.dto.'
  'cn.guet.soft_manage.biz.pojo.dto.workspace.' = 'cn.guet.soft_manage.biz.workspace.dto.'
  'cn.guet.soft_manage.biz.pojo.dto.team.' = 'cn.guet.soft_manage.biz.team.dto.'
  'cn.guet.soft_manage.biz.pojo.dto.user.' = 'cn.guet.soft_manage.biz.user.dto.'
  'cn.guet.soft_manage.biz.pojo.dto.dict.' = 'cn.guet.soft_manage.biz.dict.dto.'
  'cn.guet.soft_manage.biz.pojo.param.UserParam' = 'cn.guet.soft_manage.biz.user.param.UserParam'
  'cn.guet.soft_manage.biz.pojo.entity.DocumentComment' = 'cn.guet.soft_manage.biz.document.entity.DocumentComment'
  'cn.guet.soft_manage.biz.pojo.entity.WorkspaceAsset' = 'cn.guet.soft_manage.biz.document.entity.WorkspaceAsset'
  'cn.guet.soft_manage.biz.pojo.entity.WeeklyReport' = 'cn.guet.soft_manage.biz.workspace.entity.WeeklyReport'
  'cn.guet.soft_manage.biz.pojo.entity.WorkspaceContent' = 'cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent'
  'cn.guet.soft_manage.biz.pojo.entity.WorkspaceLock' = 'cn.guet.soft_manage.biz.workspace.entity.WorkspaceLock'
  'cn.guet.soft_manage.biz.pojo.entity.WorkspaceNode' = 'cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode'
  'cn.guet.soft_manage.biz.pojo.entity.Workspace' = 'cn.guet.soft_manage.biz.workspace.entity.Workspace'
  'cn.guet.soft_manage.biz.pojo.entity.PlanTask' = 'cn.guet.soft_manage.biz.workspace.entity.PlanTask'
  'cn.guet.soft_manage.biz.pojo.entity.TopicApproval' = 'cn.guet.soft_manage.biz.team.entity.TopicApproval'
  'cn.guet.soft_manage.biz.pojo.entity.TeamMember' = 'cn.guet.soft_manage.biz.team.entity.TeamMember'
  'cn.guet.soft_manage.biz.pojo.entity.Team' = 'cn.guet.soft_manage.biz.team.entity.Team'
  'cn.guet.soft_manage.biz.pojo.entity.DictValue' = 'cn.guet.soft_manage.biz.dict.entity.DictValue'
  'cn.guet.soft_manage.biz.pojo.entity.DictKey' = 'cn.guet.soft_manage.biz.dict.entity.DictKey'
  'cn.guet.soft_manage.biz.pojo.entity.User' = 'cn.guet.soft_manage.biz.user.entity.User'
  'cn.guet.soft_manage.biz.service.impl.WorkspaceNodeServiceImpl' = 'cn.guet.soft_manage.biz.workspace.service.impl.WorkspaceNodeServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.WorkspaceAccessServiceImpl' = 'cn.guet.soft_manage.biz.workspace.service.impl.WorkspaceAccessServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.WorkspaceServiceImpl' = 'cn.guet.soft_manage.biz.workspace.service.impl.WorkspaceServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.WeeklyReportServiceImpl' = 'cn.guet.soft_manage.biz.workspace.service.impl.WeeklyReportServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.PlanTaskServiceImpl' = 'cn.guet.soft_manage.biz.workspace.service.impl.PlanTaskServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.DocumentAssetServiceImpl' = 'cn.guet.soft_manage.biz.document.service.impl.DocumentAssetServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.DocumentCommentServiceImpl' = 'cn.guet.soft_manage.biz.document.service.impl.DocumentCommentServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.DocumentServiceImpl' = 'cn.guet.soft_manage.biz.document.service.impl.DocumentServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.TeamServiceImpl' = 'cn.guet.soft_manage.biz.team.service.impl.TeamServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.UserServiceImpl' = 'cn.guet.soft_manage.biz.user.service.impl.UserServiceImpl'
  'cn.guet.soft_manage.biz.service.impl.DictServiceImpl' = 'cn.guet.soft_manage.biz.dict.service.impl.DictServiceImpl'
  'cn.guet.soft_manage.biz.service.WorkspaceNodeService' = 'cn.guet.soft_manage.biz.workspace.service.WorkspaceNodeService'
  'cn.guet.soft_manage.biz.service.WorkspaceAccessService' = 'cn.guet.soft_manage.biz.workspace.service.WorkspaceAccessService'
  'cn.guet.soft_manage.biz.service.WorkspaceService' = 'cn.guet.soft_manage.biz.workspace.service.WorkspaceService'
  'cn.guet.soft_manage.biz.service.WeeklyReportService' = 'cn.guet.soft_manage.biz.workspace.service.WeeklyReportService'
  'cn.guet.soft_manage.biz.service.PlanTaskService' = 'cn.guet.soft_manage.biz.workspace.service.PlanTaskService'
  'cn.guet.soft_manage.biz.service.DocumentAssetService' = 'cn.guet.soft_manage.biz.document.service.DocumentAssetService'
  'cn.guet.soft_manage.biz.service.DocumentCommentService' = 'cn.guet.soft_manage.biz.document.service.DocumentCommentService'
  'cn.guet.soft_manage.biz.service.DocumentService' = 'cn.guet.soft_manage.biz.document.service.DocumentService'
  'cn.guet.soft_manage.biz.service.TeamService' = 'cn.guet.soft_manage.biz.team.service.TeamService'
  'cn.guet.soft_manage.biz.service.UserService' = 'cn.guet.soft_manage.biz.user.service.UserService'
  'cn.guet.soft_manage.biz.service.DictService' = 'cn.guet.soft_manage.biz.dict.service.DictService'
  'cn.guet.soft_manage.biz.dao.WorkspaceAssetDao' = 'cn.guet.soft_manage.biz.document.dao.WorkspaceAssetDao'
  'cn.guet.soft_manage.biz.dao.DocumentCommentDao' = 'cn.guet.soft_manage.biz.document.dao.DocumentCommentDao'
  'cn.guet.soft_manage.biz.dao.WeeklyReportDao' = 'cn.guet.soft_manage.biz.workspace.dao.WeeklyReportDao'
  'cn.guet.soft_manage.biz.dao.WorkspaceContentDao' = 'cn.guet.soft_manage.biz.workspace.dao.WorkspaceContentDao'
  'cn.guet.soft_manage.biz.dao.WorkspaceLockDao' = 'cn.guet.soft_manage.biz.workspace.dao.WorkspaceLockDao'
  'cn.guet.soft_manage.biz.dao.WorkspaceNodeDao' = 'cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao'
  'cn.guet.soft_manage.biz.dao.WorkspaceDao' = 'cn.guet.soft_manage.biz.workspace.dao.WorkspaceDao'
  'cn.guet.soft_manage.biz.dao.PlanTaskDao' = 'cn.guet.soft_manage.biz.workspace.dao.PlanTaskDao'
  'cn.guet.soft_manage.biz.dao.TopicApprovalDao' = 'cn.guet.soft_manage.biz.team.dao.TopicApprovalDao'
  'cn.guet.soft_manage.biz.dao.TeamMemberDao' = 'cn.guet.soft_manage.biz.team.dao.TeamMemberDao'
  'cn.guet.soft_manage.biz.dao.TeamDao' = 'cn.guet.soft_manage.biz.team.dao.TeamDao'
  'cn.guet.soft_manage.biz.dao.DictValueDao' = 'cn.guet.soft_manage.biz.dict.dao.DictValueDao'
  'cn.guet.soft_manage.biz.dao.DictKeyDao' = 'cn.guet.soft_manage.biz.dict.dao.DictKeyDao'
  'cn.guet.soft_manage.biz.dao.UserDao' = 'cn.guet.soft_manage.biz.user.dao.UserDao'
  'cn.guet.soft_manage.biz.utils.WorkspaceContentMetadataUtil' = 'cn.guet.soft_manage.biz.document.util.WorkspaceContentMetadataUtil'
  'cn.guet.soft_manage.biz.utils.DocumentAssetValidator' = 'cn.guet.soft_manage.biz.document.util.DocumentAssetValidator'
  'cn.guet.soft_manage.biz.utils.DocumentAssetUrlHelper' = 'cn.guet.soft_manage.biz.document.util.DocumentAssetUrlHelper'
  'cn.guet.soft_manage.biz.utils.CollabTokenUtil' = 'cn.guet.soft_manage.biz.document.util.CollabTokenUtil'
  'cn.guet.soft_manage.biz.utils.IsoWeekUtil' = 'cn.guet.soft_manage.biz.workspace.util.IsoWeekUtil'
  'cn.guet.soft_manage.biz.utils.RedissonUtil' = 'cn.guet.soft_manage.biz.common.util.RedissonUtil'
  'cn.guet.soft_manage.biz.controller.WorkspaceNodeController' = 'cn.guet.soft_manage.biz.workspace.controller.WorkspaceNodeController'
  'cn.guet.soft_manage.biz.controller.WorkspaceController' = 'cn.guet.soft_manage.biz.workspace.controller.WorkspaceController'
  'cn.guet.soft_manage.biz.controller.WeeklyReportController' = 'cn.guet.soft_manage.biz.workspace.controller.WeeklyReportController'
  'cn.guet.soft_manage.biz.controller.PlanTaskController' = 'cn.guet.soft_manage.biz.workspace.controller.PlanTaskController'
  'cn.guet.soft_manage.biz.controller.CollabInternalController' = 'cn.guet.soft_manage.biz.document.controller.CollabInternalController'
  'cn.guet.soft_manage.biz.controller.AssetProxyController' = 'cn.guet.soft_manage.biz.document.controller.AssetProxyController'
  'cn.guet.soft_manage.biz.controller.DocumentAssetController' = 'cn.guet.soft_manage.biz.document.controller.DocumentAssetController'
  'cn.guet.soft_manage.biz.controller.DocumentCommentController' = 'cn.guet.soft_manage.biz.document.controller.DocumentCommentController'
  'cn.guet.soft_manage.biz.controller.DocumentController' = 'cn.guet.soft_manage.biz.document.controller.DocumentController'
  'cn.guet.soft_manage.biz.controller.TeamController' = 'cn.guet.soft_manage.biz.team.controller.TeamController'
  'cn.guet.soft_manage.biz.controller.UserController' = 'cn.guet.soft_manage.biz.user.controller.UserController'
  'cn.guet.soft_manage.biz.controller.DictController' = 'cn.guet.soft_manage.biz.dict.controller.DictController'
}

Get-ChildItem -Path $javaRoot -Recurse -Filter *.java | ForEach-Object {
  $content = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
  $new = $content
  foreach ($k in $replacements.Keys) {
    $new = $new.Replace($k, $replacements[$k])
  }
  if ($new -ne $content) {
    [System.IO.File]::WriteAllText($_.FullName, $new, $utf8NoBom)
  }
}

# Remove empty legacy directories
@("controller", "service", "dao", "utils", "pojo") | ForEach-Object {
  $dir = Join-Path $bizRoot $_
  if (Test-Path $dir) {
    Get-ChildItem -Path $dir -Recurse -Force | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
  }
}

Write-Host "Domain migration complete."
