import { createRouter, createWebHistory, RouteLocationNormalized, RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
    {
        name: "index",
        path: "/:driverKey?/:path(.*)*",
        component: () => import("@/components/index/IndexRoute.vue"),
        meta: { title: "JDisk|首页" }
    },
    {
        name: "admin",
        path: "/admin",
        component: () => import("@/components/admin/AdminRoute.vue"),
        meta: { title: "JDisk|管理员" }
    },
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