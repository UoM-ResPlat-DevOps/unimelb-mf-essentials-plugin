# role namespace
set ROLE_NAMESPACE "unimelb"
authorization.role.namespace.create :namespace ${ROLE_NAMESPACE} :ifexists ignore

# role: unimelb:token-downloader
set TOKEN_DOWNLOADER_ROLE "${ROLE_NAMESPACE}:token-downloader"
authorization.role.create :role ${TOKEN_DOWNLOADER_ROLE} :ifexists ignore
actor.grant :type role :name ${TOKEN_DOWNLOADER_ROLE} :perm < :resource -type service asset.query :access ACCESS >
actor.grant :type role :name ${TOKEN_DOWNLOADER_ROLE} :perm < :resource -type service asset.get :access ACCESS >
actor.grant :type role :name ${TOKEN_DOWNLOADER_ROLE} :perm < :resource -type service asset.content.get :access ACCESS >
actor.grant :type role :name ${TOKEN_DOWNLOADER_ROLE} :role -type role user 

# plugin service permissions
actor.grant :type plugin:service :name unimelb.asset.download.aterm.script.create :role -type role service-user
actor.grant :type plugin:service :name unimelb.asset.download.aterm.script.create :perm < :resource -type role:namespace unimelb: :access ADMINISTER >
# TODO remove after server fix
actor.grant :type plugin:service :name unimelb.asset.download.aterm.script.create :role -type role system-administrator

actor.grant :type plugin:service :name unimelb.asset.download.shell.script.create :role -type role service-user
actor.grant :type plugin:service :name unimelb.asset.download.shell.script.create :perm < :resource -type role:namespace unimelb: :access ADMINISTER >
# TODO remove after server fix
actor.grant :type plugin:service :name unimelb.asset.download.shell.script.create :role -type role system-administrator

actor.grant :type plugin:service :name unimelb.asset.download.aterm.script.url.create :role -type role service-user
actor.grant :type plugin:service :name unimelb.asset.download.aterm.script.url.create :perm < :resource -type role:namespace unimelb: :access ADMINISTER >
# TODO remove after server fix
actor.grant :type plugin:service :name unimelb.asset.download.aterm.script.url.create :role -type role system-administrator

actor.grant :type plugin:service :name unimelb.asset.download.shell.script.url.create :role -type role service-user
actor.grant :type plugin:service :name unimelb.asset.download.shell.script.url.create :perm < :resource -type role:namespace unimelb: :access ADMINISTER >
# TODO remove after server fix
actor.grant :type plugin:service :name unimelb.asset.download.shell.script.url.create :role -type role system-administrator
