<template>
    <body>
    <div class="centered box">
        <div>
            <img style="float:left; width: 10%; margin: 4% 2%" src="../assets/logo.ico" alt="SeaSearch Logo"/>
            <span style="float:left; vertical-align:middle" class="profile_title_grad noselect">SeaSearch</span>
            <button style="margin: 6% 2%;" class="button" @click="logout">Logout</button>
        </div>
        <hr>
        <div>
            <label class="button noselect" :style="showDm ? null : disabledStyle ">
                <input v-model="showDm" type="checkbox" style="width: 0">Private
            </label>
            <label class="button noselect" :style="showGroups ? null : disabledStyle ">
                <input v-model="showGroups" type="checkbox" style="width: 0">Group
            </label>
        </div>
        <input class="search_box" v-model="filterText" placeholder="Filter by name">
        <div style="margin-top: 100px; max-height: 75%; overflow-y:scroll; display:block">
            <table class="table" style="width: 90%">
                <tbody>
                <tr v-for="item in filteredData" :key="item.title" @click="viewProfile(item.title)"
                    style="cursor: pointer;">
                    <td v-if="item.img">
                        <img v-bind:src="item.img" style="width: 4vw; vertical-align: middle;border-radius: 50%"
                             alt="Chat icon">
                    </td>
                    <td v-else>
                        <img src="../assets/telegram-app.svg" style="width: 4vw; vertical-align: middle"
                             alt="Chat icon">
                    </td>
                    <td style="width: 500px;font-size: 30px; text-align: left; vertical-align: middle">{{
                            item.title
                        }}
                    </td>
                    <td><img src="../assets/chevron.svg" style="width: 1.3vw; margin-top:25px; vertical-align: middle"
                             alt="right arrow chevron"/></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    </body>
</template>

<script>
import UserService from "../services/user.service";

export default {
    name: "ChatList",
    data() {
        return {
            showDm: true,
            showGroups: true,
            filterText: "",
            disabledStyle: {
                background: "gray"
            },
            chats: []
        }
    },
    beforeCreate() {
        UserService.getChats().then(
            res => {
                this.chats = res.data
                this.chats.forEach(it => {
                    if (it.base64Image) {
                        UserService.getImage(it.base64Image).then(res => {
                            const reader = new FileReader()
                            let raw;
                            reader.onloadend = () => {
                                it.img = reader.result;
                                console.log(raw)
                            }
                            reader.readAsDataURL(res.data)
                        })
                    }
                })
            }
        ).catch(err => console.log(err))
        this.showDm = true;
        this.showGroups = true;

    },
    computed: {
        filteredData() {
            return this.chats.length > 0 ?
                this.chats.filter(it =>
                    it.title.includes(this.filterText) && ((!it.private && this.showGroups) || (it.private && this.showDm))
                ).sort((a, b) => (a.updatedInstant > b.updatedInstant) ? -1 : ((b.updatedInstant > a.updatedInstant) ? 1 : 0)) :
                []
        }
    },
    methods: {
        logout() {
            // this.$store.dispatch("auth/logout")
            // this.$router.push("/login")
            console.log(this.chats)
        },
        viewProfile(index) {
            console.log(`Going to ${index}`)
        }
    }
}
</script>

<style scoped>
@import '../assets/styles/base.css';
@import "../assets/styles/profile.css";
</style>