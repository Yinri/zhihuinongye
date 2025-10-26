<template>
    <view>
        <!--标题和返回-->
		<cu-custom :bgColor="NavBarColor" isBack :backRouterName="backRouteName">
			<block slot="backText">返回</block>
			<block slot="content">土壤肥力表</block>
		</cu-custom>
		 <!--表单区域-->
		<view>
			<form>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">地块ID：</text></view>
                  <input type="number" placeholder="请输入地块ID" v-model="model.plotId"/>
                </view>
              </view>
              <my-date label="检测日期：" fields="day" v-model="model.testDate" placeholder="请输入检测日期"></my-date>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">pH值：</text></view>
                  <input type="number" placeholder="请输入pH值" v-model="model.phValue"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">有机质含量(g/kg)：</text></view>
                  <input type="number" placeholder="请输入有机质含量(g/kg)" v-model="model.organicMatter"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">氮含量(mg/kg)：</text></view>
                  <input type="number" placeholder="请输入氮含量(mg/kg)" v-model="model.nitrogen"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">磷含量(mg/kg)：</text></view>
                  <input type="number" placeholder="请输入磷含量(mg/kg)" v-model="model.phosphorus"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">钾含量(mg/kg)：</text></view>
                  <input type="number" placeholder="请输入钾含量(mg/kg)" v-model="model.potassium"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">肥力等级：</text></view>
                  <input  placeholder="请输入肥力等级" v-model="model.fertilityLevel"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">删除标志（0-正常，1-删除）：</text></view>
                  <input type="number" placeholder="请输入删除标志（0-正常，1-删除）" v-model="model.delFlag"/>
                </view>
              </view>
				<view class="padding">
					<button class="cu-btn block bg-blue margin-tb-sm lg" @click="onSubmit">
						<text v-if="loading" class="cuIcon-loading2 cuIconfont-spin"></text>提交
					</button>
				</view>
			</form>
		</view>
    </view>
</template>

<script>
    import myDate from '@/components/my-componets/my-date.vue'

    export default {
        name: "YoucaiSoilFertilityForm",
        components:{ myDate },
        props:{
          formData:{
              type:Object,
              default:()=>{},
              required:false
          }
        },
        data(){
            return {
				CustomBar: this.CustomBar,
				NavBarColor: this.NavBarColor,
				loading:false,
                model: {},
                backRouteName:'index',
                url: {
                  queryById: "/youcai/youcaiSoilFertility/queryById",
                  add: "/youcai/youcaiSoilFertility/add",
                  edit: "/youcai/youcaiSoilFertility/edit",
                },
            }
        },
        created(){
             this.initFormData();
        },
        methods:{
           initFormData(){
               if(this.formData){
                    let dataId = this.formData.dataId;
                    this.$http.get(this.url.queryById,{params:{id:dataId}}).then((res)=>{
                        if(res.data.success){
                            console.log("表单数据",res);
                            this.model = res.data.result;
                        }
                    })
                }
            },
            onSubmit() {
                let myForm = {...this.model};
                this.loading = true;
                let url = myForm.id?this.url.edit:this.url.add;
				this.$http.post(url,myForm).then(res=>{
				   console.log("res",res)
				   this.loading = false
				   this.$Router.push({name:this.backRouteName})
				}).catch(()=>{
					this.loading = false
				});
            }
        }
    }
</script>
