import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
    {
        name: "index",
        path: "/:driverKey?/:path(.*)*",
        component: () => import("@/components/index/IndexRoute.vue"),
        meta: { title: "JDisk|首页" },
        props: route => {
            let driverKey;
            if (route.params.driverKey === "") {
                driverKey = undefined;
            } else {
                driverKey = route.params.driverKey;
            }

            let path;
            if (route.params.path instanceof Array) {
                path = route.params.path;
            } else {
                path = undefined;
            }


            return {
                driverKey, path
            }
        }
    },
    {
        name: "login",
        path: "/login",
        component: () => import("@/components/admin/LoginRoute.vue"),
        meta: { title: "JDisk|登录" },
    },
    {
        name: "admin",
        path: "/admin",
        component: () => import("@/components/admin/AdminRoute.vue"),
        meta: { title: "JDisk|管理员" },
        props: route => ({
            selectedKey: route.meta.selectedKey
        }),
        children: [
            {
                name: "admin_normal",
                path: "normal",
                component: () => import("@/components/admin/components/normal/Normal.vue"),
                meta: { selectedKey: "normal" }
            },
            {
                name: "admin_driverList",
                path: "driver-list",
                component: () => import("@/components/admin/components/driver/DriverList.vue"),
                meta: { selectedKey: "driverList" }
            },
            {
                name: "admin_driverEdit",
                path: "driver-edit/:driverId?",
                component: () => import("@/components/admin/components/driver/DriverEdit.vue"),
                meta: { selectedKey: "driverEdit" },
                props: route => {
                    if (route.params.driverId) {
                        return {
                            id: Number(route.params.driverId)
                        }
                    }
                }
            },
            {
                name: "admin_display",
                path: "display",
                component: () => import("@/components/admin/components/display/Display.vue"),
                meta: { selectedKey: "display" }
            },
            {
                name: "admin_directLink",
                path: "direct-link",
                component: () => import("@/components/admin/components/direct-link/DirectLink.vue"),
                meta: { selectedKey: "directLink" }
            },
            {
                name: "admin_updatePassword",
                path: "update-password",
                component: () => import("@/components/admin/components/update-password/UpdatePassword.vue"),
                meta: { selectedKey: "updatePassword" }
            }
        ]
    }
]

if (Object.hasOwn === undefined) {
    Object.hasOwn = (obj, p) => {
        return obj.hasOwnProperty(p);
    }
}

const router = createRouter({
    history: createWebHistory(),
    routes: routes
});

router.beforeEach(function (to, from, next) {
    const title = to.meta.title;
    if (title && typeof title === "string") {
        document.title = title;
    } else {
        document.title = "JDisk";
    }
    next();
})

export default router;