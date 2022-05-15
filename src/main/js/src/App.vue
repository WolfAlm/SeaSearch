<template>

    <router-view class="zoomedElement"/>
</template>

<script>


export default {
    name: "Root",
    created() {
        this.$router.push("/login")
    },
    metaInfo: {
        title: 'SeaSearch',
        htmlAttrs: {
            lang: 'en-US'
        },
    },
}
</script>

<style>
@import './assets/styles/base.css';

</style>