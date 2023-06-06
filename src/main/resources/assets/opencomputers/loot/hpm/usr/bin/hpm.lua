
local JD=load([===[
local rA5U=20160728.17
local Uc06="-[ JSON.lua package by Jeffrey Friedl (http://regex.info/blog/lua/json) version 20160728.17 ]-"local lcBL={VERSION=rA5U,AUTHOR_NOTE=Uc06}local DHPxI="  "
local dx={pretty=true,align_keys=false,indent=DHPxI}
local RRuSHnxf={__tostring=function()return"JSON array"end}RRuSHnxf.__index=RRuSHnxf
local mcYOuT={__tostring=function()return"JSON object"end}mcYOuT.__index=mcYOuT;function lcBL:newArray(iXxD6s)
return setmetatable(iXxD6s or{},RRuSHnxf)end;function lcBL:newObject(oiY)return
setmetatable(oiY or{},mcYOuT)end;local function Rr(FsYIVlkf)
return
type(FsYIVlkf)=='number'and FsYIVlkf or FsYIVlkf.N end
local scRP0={__index=isNumber,__tostring=function(HLXS0Q_)return HLXS0Q_.S end,__unm=function(Kw)return
Rr(Kw)end,__concat=function(nvaIsNv7,vDnoL55)
return tostring(nvaIsNv7)..tostring(vDnoL55)end,__add=function(xlAK,zr1y)return Rr(xlAK)+Rr(zr1y)end,__sub=function(Hs,jk)return
Rr(Hs)-Rr(jk)end,__mul=function(qzSFyIO,Z65)
return Rr(qzSFyIO)*Rr(Z65)end,__div=function(umyCNfj,FT)return Rr(umyCNfj)/Rr(FT)end,__mod=function(YVLXYq,bJfct)return
Rr(YVLXYq)%Rr(bJfct)end,__pow=function(OhuFpq_N,Dzg)return
Rr(OhuFpq_N)^Rr(Dzg)end,__lt=function(_4O,C)return Rr(_4O)<Rr(C)end,__eq=function(fLI2zRe,_Fr2YU)return
Rr(fLI2zRe)==Rr(_Fr2YU)end,__le=function(Xfn,U)return
Rr(Xfn)<=Rr(U)end}
function lcBL:asNumber(Ebsw)
if getmetatable(Ebsw)==scRP0 then return Ebsw elseif
type(Ebsw)=='table'and
type(Ebsw.S)=='string'and type(Ebsw.N)=='number'then return setmetatable(Ebsw,scRP0)else
local UlikV={S=tostring(Ebsw),N=tonumber(Ebsw)}return setmetatable(UlikV,scRP0)end end
local function AI0R2TQ6(JtAjijkG)
if JtAjijkG<=127 then return string.char(JtAjijkG)elseif JtAjijkG<=2047 then
local s=math.floor(JtAjijkG/0x40)local YAtG_LV3=JtAjijkG- (0x40*s)return
string.char(0xC0+s,0x80+YAtG_LV3)elseif JtAjijkG<=65535 then
local LfEJbh_=math.floor(JtAjijkG/0x1000)local JD=JtAjijkG-0x1000*LfEJbh_
local u=math.floor(JD/0x40)local pzDMZwG=JD-0x40*u;LfEJbh_=0xE0+LfEJbh_;u=0x80+u
pzDMZwG=0x80+pzDMZwG
if

(LfEJbh_==0xE0 and u<0xA0)or
(LfEJbh_==0xED and u>0x9F)or(LfEJbh_==0xF0 and u<0x90)or(LfEJbh_==0xF4 and u>0x8F)then return"?"else return string.char(LfEJbh_,u,pzDMZwG)end else local XPoQB=math.floor(JtAjijkG/0x40000)
local XxJ=JtAjijkG-0x40000*XPoQB;local o5sms=math.floor(XxJ/0x1000)
XxJ=XxJ-0x1000*o5sms;local JQi1jg=math.floor(XxJ/0x40)local wVzn=XxJ-0x40*JQi1jg
return string.char(
0xF0+XPoQB,0x80+o5sms,0x80+JQi1jg,0x80+wVzn)end end
function lcBL:onDecodeError(pE,RSjapQ,QJf,zC)if RSjapQ then
if QJf then
pE=string.format("%s at char %d of: %s",pE,QJf,RSjapQ)else pE=string.format("%s: %s",pE,RSjapQ)end end
if zC~=nil then pE=pE.." ("..
lcBL:encode(zC)..")"end
if self.assert then self.assert(false,pE)else assert(false,pE)end end;lcBL.onDecodeOfNilError=lcBL.onDecodeError
lcBL.onDecodeOfHTMLError=lcBL.onDecodeError
function lcBL:onEncodeError(pfZ3SPy_,pDNa2ox6)
if pDNa2ox6 ~=nil then pfZ3SPy_=pfZ3SPy_..
" ("..lcBL:encode(pDNa2ox6)..")"end;if self.assert then self.assert(false,pfZ3SPy_)else
assert(false,pfZ3SPy_)end end
local function yA(Do6yo7nm,y06X3k,ivnJjrA,d3fMjkg)
local el=y06X3k:match('^-?[1-9]%d*',ivnJjrA)or y06X3k:match("^-?0",ivnJjrA)if not el then
Do6yo7nm:onDecodeError("expected number",y06X3k,ivnJjrA,d3fMjkg.etc)end
local Wu_uIt=ivnJjrA+el:len()local w=y06X3k:match('^%.%d+',Wu_uIt)or""Wu_uIt=Wu_uIt+
w:len()
local sgeP=y06X3k:match('^[eE][-+]?%d+',Wu_uIt)or""Wu_uIt=Wu_uIt+sgeP:len()local CM=el..w..sgeP;if
d3fMjkg.decodeNumbersAsObjects then return lcBL:asNumber(CM),Wu_uIt end
if

(
d3fMjkg.decodeIntegerStringificationLength and(el:len()>=d3fMjkg.decodeIntegerStringificationLength or
sgeP:len()>0))or
(d3fMjkg.decodeDecimalStringificationLength and
(
w:len()>=d3fMjkg.decodeDecimalStringificationLength or sgeP:len()>0))then return CM,Wu_uIt end;local Qlmlet=tonumber(CM)if not Qlmlet then
Do6yo7nm:onDecodeError("bad number",y06X3k,ivnJjrA,d3fMjkg.etc)end;return Qlmlet,Wu_uIt end
local function XmVolesU(_,RkGFh6,hw18,nvCiFt7r)if RkGFh6:sub(hw18,hw18)~='"'then
_:onDecodeError("expected string's opening quote",RkGFh6,hw18,nvCiFt7r.etc)end;local xSebv5Jc=hw18+1
local mMp=RkGFh6:len()local rDtVf=""
while xSebv5Jc<=mMp do local vj=RkGFh6:sub(xSebv5Jc,xSebv5Jc)if
vj=='"'then return rDtVf,xSebv5Jc+1 end
if vj~='\\'then rDtVf=rDtVf..vj;xSebv5Jc=
xSebv5Jc+1 elseif RkGFh6:match('^\\b',xSebv5Jc)then rDtVf=rDtVf.."\b"xSebv5Jc=
xSebv5Jc+2 elseif RkGFh6:match('^\\f',xSebv5Jc)then rDtVf=rDtVf.."\f"xSebv5Jc=
xSebv5Jc+2 elseif RkGFh6:match('^\\n',xSebv5Jc)then rDtVf=rDtVf.."\n"xSebv5Jc=
xSebv5Jc+2 elseif RkGFh6:match('^\\r',xSebv5Jc)then rDtVf=rDtVf.."\r"xSebv5Jc=
xSebv5Jc+2 elseif RkGFh6:match('^\\t',xSebv5Jc)then rDtVf=rDtVf.."\t"xSebv5Jc=
xSebv5Jc+2 else
local z=RkGFh6:match('^\\u([0123456789aAbBcCdDeEfF][0123456789aAbBcCdDeEfF][0123456789aAbBcCdDeEfF][0123456789aAbBcCdDeEfF])',xSebv5Jc)
if z then xSebv5Jc=xSebv5Jc+6;local Zg=tonumber(z,16)
if
Zg>=0xD800 and Zg<=0xDBFF then
local ykRppH=RkGFh6:match('^\\u([dD][cdefCDEF][0123456789aAbBcCdDeEfF][0123456789aAbBcCdDeEfF])',xSebv5Jc)
if ykRppH then xSebv5Jc=xSebv5Jc+6;Zg=0x2400+ (Zg-0xD800)*0x400+
tonumber(ykRppH,16)else end end;rDtVf=rDtVf..AI0R2TQ6(Zg)else rDtVf=rDtVf..
RkGFh6:match('^\\(.)',xSebv5Jc)xSebv5Jc=xSebv5Jc+2 end end end
_:onDecodeError("unclosed string",RkGFh6,hw18,nvCiFt7r.etc)end
local function eZ0l3ch(WQ6,y36Aetn)local iPL3B4cr,GI2hz6SK=WQ6:find("^[ \n\r\t]+",y36Aetn)if GI2hz6SK then
return GI2hz6SK+1 else return y36Aetn end end;local W_63_9
local function h9dyA_4T(Oh,PG,n,O)if PG:sub(n,n)~='{'then
Oh:onDecodeError("expected '{'",PG,n,O.etc)end;local N5UjTN=eZ0l3ch(PG,n+1)local qLH5=Oh.strictTypes and
Oh:newObject{}or{}if
PG:sub(N5UjTN,N5UjTN)=='}'then return qLH5,N5UjTN+1 end
local tE=PG:len()
while N5UjTN<=tE do local VcV0EuD,pX4gCR=XmVolesU(Oh,PG,N5UjTN,O)
N5UjTN=eZ0l3ch(PG,pX4gCR)if PG:sub(N5UjTN,N5UjTN)~=':'then
Oh:onDecodeError("expected colon",PG,N5UjTN,O.etc)end
N5UjTN=eZ0l3ch(PG,N5UjTN+1)local gad4ZcL,pX4gCR=W_63_9(Oh,PG,N5UjTN,O)qLH5[VcV0EuD]=gad4ZcL
N5UjTN=eZ0l3ch(PG,pX4gCR)local dk=PG:sub(N5UjTN,N5UjTN)
if dk=='}'then return qLH5,N5UjTN+1 end;if PG:sub(N5UjTN,N5UjTN)~=','then
Oh:onDecodeError("expected comma or '}'",PG,N5UjTN,O.etc)end
N5UjTN=eZ0l3ch(PG,N5UjTN+1)end;Oh:onDecodeError("unclosed '{'",PG,n,O.etc)end
local function oh(E,OO,y,cR6rJlAl)if OO:sub(y,y)~='['then
E:onDecodeError("expected '['",OO,y,cR6rJlAl.etc)end;local M6ilzGJ=eZ0l3ch(OO,y+1)local iW6CD=E.strictTypes and
E:newArray{}or{}if
OO:sub(M6ilzGJ,M6ilzGJ)==']'then return iW6CD,M6ilzGJ+1 end
local wZdg=1;local BaX=OO:len()
while M6ilzGJ<=BaX do
local SJsW11k,Ki1HJT=W_63_9(E,OO,M6ilzGJ,cR6rJlAl)iW6CD[wZdg]=SJsW11k;wZdg=wZdg+1;M6ilzGJ=eZ0l3ch(OO,Ki1HJT)
local wjim8xCV=OO:sub(M6ilzGJ,M6ilzGJ)if wjim8xCV==']'then return iW6CD,M6ilzGJ+1 end;if
OO:sub(M6ilzGJ,M6ilzGJ)~=','then
E:onDecodeError("expected comma or '['",OO,M6ilzGJ,cR6rJlAl.etc)end
M6ilzGJ=eZ0l3ch(OO,M6ilzGJ+1)end
E:onDecodeError("unclosed '['",OO,y,cR6rJlAl.etc)end
W_63_9=function(E,QLam,qTDt,v)qTDt=eZ0l3ch(QLam,qTDt)
if qTDt>QLam:len()then E:onDecodeError("unexpected end of string",QLam,
nil,v.etc)end
if QLam:find('^"',qTDt)then return XmVolesU(E,QLam,qTDt,v.etc)elseif
QLam:find('^[-0123456789 ]',qTDt)then return yA(E,QLam,qTDt,v)elseif QLam:find('^%{',qTDt)then
return h9dyA_4T(E,QLam,qTDt,v)elseif QLam:find('^%[',qTDt)then return oh(E,QLam,qTDt,v)elseif QLam:find('^true',qTDt)then return true,
qTDt+4 elseif QLam:find('^false',qTDt)then return false,qTDt+5 elseif
QLam:find('^null',qTDt)then return nil,qTDt+4 else
E:onDecodeError("can't parse JSON",QLam,qTDt,v.etc)end end
function lcBL:decode(Ta,u,nArcvQl)if type(nArcvQl)~='table'then nArcvQl={}end;if u~=nil then
nArcvQl.etc=u end;if
type(self)~='table'or self.__index~=lcBL then
lcBL:onDecodeError("JSON:decode must be called in method format",nil,nil,nArcvQl.etc)end
if Ta==nil then
self:onDecodeOfNilError(string.format("nil passed to JSON:decode()"),
nil,nil,nArcvQl.etc)elseif type(Ta)~='string'then
self:onDecodeError(string.format("expected string argument to JSON:decode(), got %s",type(Ta)),
nil,nil,nArcvQl.etc)end;if Ta:match('^%s*$')then return nil end;if Ta:match('^%s*<')then
self:onDecodeOfHTMLError(string.format("html passed to JSON:decode()"),Ta,
nil,nArcvQl.etc)end
if
Ta:sub(1,1):byte()==0 or
(Ta:len()>=2 and Ta:sub(2,2):byte()==0)then
self:onDecodeError("JSON package groks only UTF-8, sorry",Ta,nil,nArcvQl.etc)end;if nArcvQl.decodeNumbersAsObjects==nil then
nArcvQl.decodeNumbersAsObjects=self.decodeNumbersAsObjects end;if
nArcvQl.decodeIntegerStringificationLength==nil then
nArcvQl.decodeIntegerStringificationLength=self.decodeIntegerStringificationLength end;if
nArcvQl.decodeDecimalStringificationLength==nil then
nArcvQl.decodeDecimalStringificationLength=self.decodeDecimalStringificationLength end
local h6Ub7U,Gm=pcall(W_63_9,self,Ta,1,nArcvQl)if h6Ub7U then return Gm else
if self.assert then self.assert(false,Gm)else assert(false,Gm)end;return nil,Gm end end
local function DZXGTh(YKA7cU)
if YKA7cU=="\n"then return"\\n"elseif YKA7cU=="\r"then return"\\r"elseif YKA7cU=="\t"then return"\\t"elseif YKA7cU=="\b"then
return"\\b"elseif YKA7cU=="\f"then return"\\f"elseif YKA7cU=='"'then return'\\"'elseif YKA7cU=='\\'then return'\\\\'else return
string.format("\\u%04x",YKA7cU:byte())end end
local Su9Koz='['..'"'..
'%\\'..'%z'..'\001'..'-'..'\031'..']'local Uk7e=AI0R2TQ6(0x2028)local KwQCk_G=AI0R2TQ6(0x2029)
local function ptZa(mCsewfX,yY)
local Xf=mCsewfX:gsub(Su9Koz,DZXGTh)if yY.stringsAreUtf8 then
Xf=Xf:gsub(Uk7e,'\\u2028'):gsub(KwQCk_G,'\\u2029')end;return'"'..Xf..'"'end
local function PEqsd(UlFdiZ7v,U,wFeA)local JQgI={}local N={}local fs52REi=false;local PUNkgaiM
for X in pairs(U)do
if type(X)=='string'then
table.insert(JQgI,X)elseif type(X)=='number'then table.insert(N,X)
if X<=0 or X>=math.huge then
fs52REi=true elseif not PUNkgaiM or X>PUNkgaiM then PUNkgaiM=X end else
UlFdiZ7v:onEncodeError("can't encode table with a key of type "..type(X),wFeA)end end
if#JQgI==0 and not fs52REi then
if#N>0 then return nil,PUNkgaiM elseif
tostring(U)=="JSON array"then return nil elseif tostring(U)=="JSON object"then return{}else return nil end end;table.sort(JQgI)local s6FbB
if#N>0 then if UlFdiZ7v.noKeyConversion then
UlFdiZ7v:onEncodeError("a table with both numeric and string keys could be an object or array; aborting",wFeA)end
s6FbB={}for dc61,aguhyl in pairs(U)do s6FbB[dc61]=aguhyl end
table.sort(N)
for p,gOPDv in ipairs(N)do local aSdZU3=tostring(gOPDv)
if s6FbB[aSdZU3]==nil then
table.insert(JQgI,aSdZU3)s6FbB[aSdZU3]=U[gOPDv]else
UlFdiZ7v:onEncodeError(
"conflict converting table with mixed-type keys into a JSON object: key "..gOPDv.." exists both as a string and a number.",wFeA)end end end;return JQgI,nil,s6FbB end;local iSj
function iSj(YKDL,oFyb6OLp,oGdh_mv,WjvvK,TASVwBgU,KjUncMB,XkT)
if oFyb6OLp==nil or
(not XkT and TASVwBgU and TASVwBgU.null and oFyb6OLp==
TASVwBgU.null)then return'null'elseif
type(oFyb6OLp)=='string'then return ptZa(oFyb6OLp,TASVwBgU)elseif type(oFyb6OLp)=='number'then
if
oFyb6OLp~=oFyb6OLp then return"null"elseif oFyb6OLp>=math.huge then return"1e+9999"elseif oFyb6OLp<=-math.huge then
return"-1e+9999"else return tostring(oFyb6OLp)end elseif type(oFyb6OLp)=='boolean'then return tostring(oFyb6OLp)elseif type(oFyb6OLp)~=
'table'then
YKDL:onEncodeError("can't convert "..type(oFyb6OLp).." to JSON",WjvvK)elseif getmetatable(oFyb6OLp)==scRP0 then return tostring(oFyb6OLp)else
local c3dr=oFyb6OLp;if type(TASVwBgU)~='table'then TASVwBgU={}end;if type(KjUncMB)~=
'string'then KjUncMB=""end
if oGdh_mv[c3dr]then
YKDL:onEncodeError("table "..
tostring(c3dr).." is a child of itself",WjvvK)else oGdh_mv[c3dr]=true end;local NGH;local tIc,MD2O,HQ=PEqsd(YKDL,c3dr,WjvvK)
if MD2O then local cng={}for lE=1,MD2O do
table.insert(cng,iSj(YKDL,c3dr[lE],oGdh_mv,WjvvK,TASVwBgU,KjUncMB))end
if TASVwBgU.pretty then NGH="[ "..
table.concat(cng,", ").." ]"else NGH="["..
table.concat(cng,",").."]"end elseif tIc then local nI2F0id=HQ or c3dr
if TASVwBgU.pretty then local N4aMD_P={}local pCi=0
for lNOqUk8,YAnZNei in ipairs(tIc)do
local h8YWR44E=iSj(YKDL,tostring(YAnZNei),oGdh_mv,WjvvK,TASVwBgU,KjUncMB,true)
if TASVwBgU.align_keys then pCi=math.max(pCi,#h8YWR44E)end;table.insert(N4aMD_P,h8YWR44E)end
local NzeoQJ=KjUncMB..tostring(TASVwBgU.indent or"")
local AwGfFV=NzeoQJ..string.rep(" ",pCi)..
(TASVwBgU.align_keys and"  "or"")
local wCRY="%s%"..string.format("%d",pCi).."s: %s"local d0uKSVw1={}
for VF,fTrMe in ipairs(tIc)do
local ypDndT8=iSj(YKDL,nI2F0id[fTrMe],oGdh_mv,WjvvK,TASVwBgU,AwGfFV)
table.insert(d0uKSVw1,string.format(wCRY,NzeoQJ,N4aMD_P[VF],ypDndT8))end;NGH="{\n"..
table.concat(d0uKSVw1,",\n").."\n"..KjUncMB.."}"else local MV65={}
for Y3D66Ym9,q in
ipairs(tIc)do
local PhJ=iSj(YKDL,nI2F0id[q],oGdh_mv,WjvvK,TASVwBgU,KjUncMB)
local h=iSj(YKDL,tostring(q),oGdh_mv,WjvvK,TASVwBgU,KjUncMB,true)
table.insert(MV65,string.format("%s:%s",h,PhJ))end;NGH="{"..table.concat(MV65,",").."}"end else NGH="[]"end;oGdh_mv[c3dr]=false;return NGH end end
function lcBL:encode(j2K,r8hgwQ,_6U)if type(self)~='table'or self.__index~=lcBL then
lcBL:onEncodeError("JSON:encode must be called in method format",r8hgwQ)end;if
type(_6U)~='table'then _6U={}end;return iSj(self,j2K,{},r8hgwQ,_6U)end
function lcBL:encode_pretty(GLSzBQs,c,xg)if type(self)~='table'or self.__index~=lcBL then
lcBL:onEncodeError("JSON:encode_pretty must be called in method format",c)end;if
type(xg)~='table'then xg=dx end;return iSj(self,GLSzBQs,{},c,xg)end;function lcBL.__tostring()return"JSON encode/decode package"end
lcBL.__index=lcBL
function lcBL:new(Id2KoP_G)local Y2or={}if Id2KoP_G then
for zN8ASHV5,iju in pairs(Id2KoP_G)do Y2or[zN8ASHV5]=iju end end
return setmetatable(Y2or,lcBL)end;return lcBL:new()
]===])()
local u=(function()
  local N=[[     Copyright (c) The python-semanticversion project
     All rights reserved.

     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this
     list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.

     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
     ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
     WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
     ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
     (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
     ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ]]
  N=[[  The use of the library is similar to the original one,
  check the documentation here: https://python-semanticversion.readthedocs.io/en/latest/
  ]]local fs52REi,PUNkgaiM,s6FbB;do local KjUncMB=table
    fs52REi,PUNkgaiM,s6FbB=KjUncMB.concat,KjUncMB.insert,KjUncMB.unpack end;local X
  X=function(XkT)
    do local c3dr=tonumber(XkT)if
    c3dr then return c3dr,true else return XkT,false end end end;local dc61
  dc61=function(NGH)return
  NGH and NGH[1]=='0'and tonumber(NGH and NGH~='0')end;local aguhyl
  aguhyl=function(tIc,MD2O)if tIc==MD2O then return 0 end;if tIc>MD2O then return 1 end;if tIc<MD2O then
    return-1 end end;local p
  p=function(HQ,cng)local lE,nI2F0id=X(HQ)local N4aMD_P,pCi=X(cng)
    if nI2F0id and pCi then
      return aguhyl(lE,N4aMD_P)elseif nI2F0id then return-1 elseif pCi then return 1 else return aguhyl(lE,N4aMD_P)end end;local gOPDv
  gOPDv=function(NzeoQJ,AwGfFV)local wCRY
    do local d0uKSVw1={}
      for lNOqUk8=1,#NzeoQJ do if AwGfFV[lNOqUk8]then
        d0uKSVw1[NzeoQJ[lNOqUk8]]=AwGfFV[lNOqUk8]end end;wCRY=d0uKSVw1 end
    for YAnZNei,h8YWR44E in pairs(wCRY)do local VF=p(YAnZNei,h8YWR44E)if VF~=0 then return VF end end;return aguhyl(#NzeoQJ,#AwGfFV)end;local aSdZU3
  do local fTrMe
    local ypDndT8={_coerce=function(Y3D66Ym9,q,PhJ)if PhJ==nil then PhJ=false end
      if q==nil and PhJ then return q end;return tonumber(q)end,next_major=function(h)
      if
      h.prerelease and h.minor==0 and h.patch==0 then
        return
        aSdZU3(fs52REi((function()local j2K={}
          local r8hgwQ=1;local _6U={h.major,h.minor,h.patch}for GLSzBQs=1,#_6U do local c=_6U[GLSzBQs]
            j2K[r8hgwQ]=tostring(c)r8hgwQ=r8hgwQ+1 end;return j2K end)(),'.'))else
        return
        aSdZU3(fs52REi((function()local xg={}local Id2KoP_G=1;local Y2or={h.major+1,0,0}
          for zN8ASHV5=1,#Y2or do
            local iju=Y2or[zN8ASHV5]xg[Id2KoP_G]=tostring(iju)Id2KoP_G=Id2KoP_G+1 end;return xg end)(),'.'))end end,next_minor=function(XsWgh)if
    not(XsWgh.minor)then
      error("Partial version doesn't contain the minor component!")end
      if
      XsWgh.prerelease and XsWgh.patch==0 then
        return
        aSdZU3(fs52REi((function()local l4Hdz={}local NSXCgSH=1
          local Wq={XsWgh.major,XsWgh.minor,XsWgh.patch}for SbOQ=1,#Wq do local IiuHGo=Wq[SbOQ]l4Hdz[NSXCgSH]=tostring(IiuHGo)
            NSXCgSH=NSXCgSH+1 end;return l4Hdz end)(),'.'))else
        return
        aSdZU3(fs52REi((function()local cGqxtYr={}local bgJFKeeZ=1
          local yu9fg0nN={XsWgh.major,XsWgh.minor+1,0}
          for wgx=1,#yu9fg0nN do local zlU7X=yu9fg0nN[wgx]
            cGqxtYr[bgJFKeeZ]=tostring(zlU7X)bgJFKeeZ=bgJFKeeZ+1 end;return cGqxtYr end)(),'.'))end end,next_patch=function(t)if
    not(t.patch)then
      error("Partial version doesn't contain the patch component!")end
      if t.prerelease then
        return
        aSdZU3(fs52REi((function()local f6qbO={}local kk=1
          local QrubIAv={t.major,t.minor,t.patch}for bLHDW=1,#QrubIAv do local YjFd7b=QrubIAv[bLHDW]f6qbO[kk]=tostring(YjFd7b)
            kk=kk+1 end;return f6qbO end)(),'.'))else
        return
        aSdZU3(fs52REi((function()local jZgPYb={}local zN2=1;local IN69pa5={t.major,t.minor,t.patch+1}for UOWJ=1,#
        IN69pa5 do local WtalJw=IN69pa5[UOWJ]jZgPYb[zN2]=tostring(WtalJw)
          zN2=zN2+1 end;return jZgPYb end)(),'.'))end end,coerce=function(JYrf2,KHDOUlRY,I0JvPpn)if
    I0JvPpn==nil then I0JvPpn=false end;local Ce4ZE
      Ce4ZE=function(kxeBp)
        local a,kQ=kxeBp:match('^(%d+)(.*)$')if not(a)then return nil end;local EE9LAE=a
        local iVx,eg=kQ:match('^%.(%d+)(.*)$')if iVx then kQ=eg;EE9LAE=EE9LAE.. ('.'..iVx)end
        local AQviNt;AQviNt,eg=kQ:match('^%.(%d+)(.*)$')if AQviNt then kQ=eg;EE9LAE=EE9LAE..
            ('.'..AQviNt)end;return kxeBp,EE9LAE end;local OVx_mN,lB=Ce4ZE(KHDOUlRY)if not(OVx_mN)then
        error("Version string lacks a numerical component: "..
            tostring(KHDOUlRY))end
      local byE=KHDOUlRY:sub(1,#lB)if not I0JvPpn then while({byE:gsub('.','')})[2]<2 do
        byE=byE..'.0'end end;if
      #lB==#KHDOUlRY then return aSdZU3(byE,I0JvPpn)end
      local bITCI=KHDOUlRY:sub(#lB+1)bITCI=bITCI:gsub('[^a-zA-Z0-9+.-]','-')local K,F5dtVpnN=nil,nil
      if
      bITCI:sub(1,1)=='+'then K=''F5dtVpnN=bITCI:sub(2)elseif bITCI:sub(1,1)=='.'then K=''
        F5dtVpnN=bITCI:sub(2)elseif bITCI:sub(1,1)=='-'then bITCI=bITCI:sub(2)
        do
          local T6=bITCI:find('+')if T6 then K,F5dtVpnN=bITCI:sub(1,T6-1),bITCI:sub(T6+1,-1)else
          K,F5dtVpnN=bITCI,''end end else do local NviN0i=bITCI:find('+')
        if NviN0i then K,F5dtVpnN=bITCI:sub(1,NviN0i-1),bITCI:sub(NviN0i+1,
            -1)else K,F5dtVpnN=bITCI,''end end end;F5dtVpnN=F5dtVpnN:gsub('+','.')if K and K~=''then
        byE=byE.. ('-'..K)end;if F5dtVpnN and F5dtVpnN~=''then
        byE=byE.. ('+'..F5dtVpnN)end;return JYrf2.__class(byE,I0JvPpn)end,parse=function(BlMQce,o,dpRE,fEiXwWq)if
    dpRE==nil then dpRE=false end;if fEiXwWq==nil then fEiXwWq=false end;if
    not o or type(o)~='string'or o==''then
      error("Invalid empty version string: "..
          tostring(tostring(o)))end;local r3JzMga6
      if dpRE then
        r3JzMga6=BlMQce.__class.partialVersionRe else r3JzMga6=BlMQce.__class.versionRe end;local Tuyw,FYLcr2nu,ioS69,AiP,S2jwpoi=r3JzMga6(BlMQce.__class,o)if not Tuyw then
        error(
            "Invalid version string: "..tostring(o))end;if dc61(Tuyw)then
        error("Invalid leading zero in major: "..tostring(o))end;if dc61(FYLcr2nu)then
        error("Invalid leading zero in minor: "..
            tostring(o))end;if dc61(ioS69)then
        error("Invalid leading zero in patch: "..
            tostring(o))end;Tuyw=tonumber(Tuyw)
      FYLcr2nu=BlMQce:_coerce(FYLcr2nu,dpRE)ioS69=BlMQce:_coerce(ioS69,dpRE)
      if AiP==nil then
        if
        dpRE and S2jwpoi==nil then return{Tuyw,FYLcr2nu,ioS69,nil,nil}else AiP={}end elseif AiP==''then AiP={}else
        do local _WX9u={}local u0riyU=1;for UH in AiP:gmatch('[^.]+')do _WX9u[u0riyU]=UH
          u0riyU=u0riyU+1 end;AiP=_WX9u end;BlMQce:_validateIdentifiers(AiP,false)end
      if S2jwpoi==nil then if dpRE then S2jwpoi=nil else S2jwpoi={}end elseif S2jwpoi==''then S2jwpoi={}else do
        local WNph={}local ytF=1
        for d in S2jwpoi:gmatch('[^.]+')do WNph[ytF]=d;ytF=ytF+1 end;S2jwpoi=WNph end
        BlMQce:_validateIdentifiers(S2jwpoi,true)end;return{Tuyw,FYLcr2nu,ioS69,AiP,S2jwpoi}end,_validateIdentifiers=function(gRm,LPX0,g)if
    g==nil then g=false end
      for _l=1,#LPX0 do local qao=LPX0[_l]if not qao then
        error("Invalid empty identifier "..

            tostring(qao).." in "..tostring(fs52REi(LPX0,'.')))end;if

      qao:sub(1,1)=='0'and tonumber(qao)and qao~='0'and not g then
        error("Invalid leading zero in identifier "..tostring(qao))end end end,__pairs=function(ipUPIzc)return
    pairs({ipUPIzc.major,ipUPIzc.minor,ipUPIzc.patch,ipUPIzc.prerelease,ipUPIzc.build})end,__ipairs=function(N8)return
    ipairs({N8.major,N8.minor,N8.patch,N8.prerelease,N8.build})end,__tostring=function(Gzk)
      local J7nsK=tostring(Gzk.major)
      if Gzk.minor~=nil then J7nsK=J7nsK.. ('.'..Gzk.minor)end
      if Gzk.patch~=nil then J7nsK=J7nsK.. ('.'..Gzk.patch)end
      if Gzk.prerelease and#Gzk.prerelease>0 or

          Gzk.partial and Gzk.prerelease and#Gzk.prerelease==0 and Gzk.build==nil then
        J7nsK=J7nsK.. ('-'..
            fs52REi(Gzk.prerelease,'.'))end
      if Gzk.build and#Gzk.build>0 or Gzk.partial and Gzk.build and
          #Gzk.build==0 then J7nsK=J7nsK.. ('+'..
          fs52REi(Gzk.build,'.'))end;return J7nsK end,_comparsionFunctions=function(dXbd,vQj)if
    vQj==nil then vQj=false end;local sVBxyy
      sVBxyy=function(bJtvRSR,aBhZK5)
        if bJtvRSR and aBhZK5 then
          return gOPDv(bJtvRSR,aBhZK5)elseif bJtvRSR then return-1 elseif aBhZK5 then return 1 else return 0 end end;local N9d
      N9d=function(Jz8JUscj,OtGmbAgE)
        if Jz8JUscj==OtGmbAgE then return 0 else return'not implemented'end end;local S7
      S7=function(oU_r)local n_lv
        n_lv=function(UYQF,WXx)
          if UYQF==nil or WXx==nil then return 0 else return oU_r(UYQF,WXx)end end;return n_lv end
      if vQj then
        return{aguhyl,S7(aguhyl),S7(aguhyl),S7(sVBxyy),S7(N9d)}else return{aguhyl,aguhyl,aguhyl,sVBxyy,N9d}end end,__compare=function(W4EuxJXi,BlYNd61h)
      local XDPndG=W4EuxJXi:_comparsionFunctions(
          W4EuxJXi.partial or BlYNd61h.partial)
      local sJYFQIP4={{XDPndG[1],W4EuxJXi.major,BlYNd61h.major},{XDPndG[2],W4EuxJXi.minor,BlYNd61h.minor},{XDPndG[3],W4EuxJXi.patch,BlYNd61h.patch},{XDPndG[4],W4EuxJXi.prerelease,BlYNd61h.prerelease},{XDPndG[5],W4EuxJXi.build,BlYNd61h.build}}
      for Ogq0S2=1,#sJYFQIP4 do local n8Cw3SR=sJYFQIP4[Ogq0S2]local GJqd7gt,slE5aDm2,aL_g=s6FbB(n8Cw3SR)
        local IMUI10L=GJqd7gt(slE5aDm2,aL_g)if IMUI10L~=0 then return IMUI10L end end;return 0 end,__compareHelper=function(vPA,pUXZ6G4,mk,OeQex1U4)
      local i0cV9=vPA:__compare(pUXZ6G4)if i0cV9 =='not implemented'then return OeQex1U4 end
      return mk(i0cV9)end,__eq=function(EGD,VWiGCreH)
      local B_kkL;B_kkL=function(uEO6Y)return uEO6Y==0 end;return
      EGD:__compareHelper(VWiGCreH,B_kkL,false)end,__lt=function(i_053JPY,l)
      local UK;UK=function(NzaICo)return NzaICo<0 end;return
      i_053JPY:__compareHelper(l,UK,false)end,__le=function(k1X83nYm,xxzxfj)
      local _ad1m4I;_ad1m4I=function(H1QsS)return H1QsS<=0 end;return
      k1X83nYm:__compareHelper(xxzxfj,_ad1m4I,false)end}ypDndT8.__index=ypDndT8
    fTrMe=setmetatable({__init=function(rIMx,TiA,Y51P)if Y51P==nil then Y51P=false end
      local ichL,NOK,Alv,YeLO2,CkrmO=s6FbB(rIMx:parse(TiA,Y51P))
      rIMx.major,rIMx.minor,rIMx.patch,rIMx.prerelease,rIMx.build,rIMx.partial=ichL,NOK,Alv,YeLO2,CkrmO,Y51P end,__base=ypDndT8,__name="Version"},{__index=ypDndT8,__call=function(ooovsSJe,...)
      local s5IsD=setmetatable({},ypDndT8)ooovsSJe.__init(s5IsD,...)return s5IsD end})ypDndT8.__class=fTrMe;local MV65=fTrMe
    MV65.versionRe=function(MV65,KvYEVoXt)
      local VWWD_P,zsMuNkv,aXxi,Q18a7QTy=KvYEVoXt:match('^(%d+)%.(%d+)%.(%d+)(.*)$')if not(VWWD_P)then return nil end
      local K5Rp6,GTIA=Q18a7QTy:match('^%-([0-9a-zA-z.-]+)(.*)$')if K5Rp6 then Q18a7QTy=GTIA end;local gdPUe
      gdPUe,GTIA=Q18a7QTy:match('^%+([0-9a-zA-Z.-]+)(.*)$')if gdPUe then Q18a7QTy=GTIA end;if#Q18a7QTy>0 then return nil end;return VWWD_P,zsMuNkv,
      aXxi,K5Rp6,gdPUe end
    MV65.partialVersionRe=function(MV65,_bxEn)local pcN_ceXY,_P=_bxEn:match('^(%d+)(.*)$')if
    not(pcN_ceXY)then return nil end;local rq,mo=_P:match('^%.(%d+)(.*)$')
      if rq then _P=mo end;local I;I,mo=_P:match('^%.(%d+)(.*)$')if I then _P=mo end;local RAAJAsR
      RAAJAsR,mo=_P:match('^%-([0-9a-zA-Z.-]*)(.*)$')if RAAJAsR then _P=mo end;local c1pjj7
      c1pjj7,mo=_P:match('^%+([0-9a-zA-Z.-]*)(.*)$')if c1pjj7 then _P=mo end;if#_P>0 then return nil end;return pcN_ceXY,rq,I,RAAJAsR,c1pjj7 end;aSdZU3=fTrMe end;local YKDL
  do local BMv
    local NQh8={parse=function(bkTe,ohmPbyDd)if not ohmPbyDd or type(ohmPbyDd)~='string'or
        ohmPbyDd==''then
      error("Invalid empty requirement specification: "..
          tostring(tostring(ohmPbyDd)))end
      if
      ohmPbyDd=='*'then return{bkTe.__class.KIND_ANY,''}end;local D,DfDLWkT=bkTe.__class:reSpec(ohmPbyDd)if not D then
        error(
            "Invalid requirement specification: "..tostring(ohmPbyDd))end;D=
      bkTe.__class.KIND_ALIASES[D]or D;local MTU8HP4d=aSdZU3(DfDLWkT,true)
      if
      MTU8HP4d.build~=nil and D~=bkTe.__class.KIND_EQUAL and D~=
          bkTe.__class.KIND_NEQ then
        error("Invalid requirement specification "..
            tostring(ohmPbyDd)..": build numbers have no ordering")end;return{D,MTU8HP4d}end,match=function(hIM_cG0i,jD)
      local me=hIM_cG0i.kind
      if hIM_cG0i.__class.KIND_ANY==me then return true elseif
      hIM_cG0i.__class.KIND_LT==me then return jD<hIM_cG0i.spec elseif hIM_cG0i.__class.KIND_LTE==me then return jD<=
          hIM_cG0i.spec elseif hIM_cG0i.__class.KIND_EQUAL==me then return
      jD==hIM_cG0i.spec elseif hIM_cG0i.__class.KIND_GTE==me then
        return jD>=hIM_cG0i.spec elseif hIM_cG0i.__class.KIND_GT==me then return jD>hIM_cG0i.spec elseif
      hIM_cG0i.__class.KIND_NEQ==me then return jD~=hIM_cG0i.spec elseif
      hIM_cG0i.__class.KIND_CARET==me then return
      hIM_cG0i.spec<=jD and jD<hIM_cG0i.spec:next_major()elseif hIM_cG0i.__class.KIND_TILDE==me then return

      hIM_cG0i.spec<=jD and jD<hIM_cG0i.spec:next_minor()else return
      error("Unexpected match kind: "..tostring(hIM_cG0i.kind))end end,__tostring=function(sgU5HAMG)
      return
      tostring(sgU5HAMG.kind)..tostring(sgU5HAMG.spec)end,__eq=function(FDydY,PEZ_)return FDydY.kind==PEZ_.kind and FDydY.spec==
        PEZ_.spec end}NQh8.__index=NQh8
    BMv=setmetatable({__init=function(c,ElbTbcZG)
      c.kind,c.spec=s6FbB(c:parse(ElbTbcZG))end,__base=NQh8,__name="SpecItem"},{__index=NQh8,__call=function(r3,...)
      local pUiVYRok=setmetatable({},NQh8)r3.__init(pUiVYRok,...)return pUiVYRok end})NQh8.__class=BMv;local P=BMv;P.KIND_ANY='*'P.KIND_LT='<'P.KIND_LTE='<='
    P.KIND_EQUAL='=='P.KIND_SHORTEQ='='P.KIND_EMPTY=''P.KIND_GTE='>='P.KIND_GT='>'
    P.KIND_NEQ='!='P.KIND_CARET='^'P.KIND_TILDE='~'
    P.KIND_ALIASES={[P.__class.KIND_SHORTEQ]=P.__class.KIND_EQUAL,[P.__class.KIND_EMPTY]=P.__class.KIND_EQUAL}
    P.reSpec=function(P,jvPsY9)local tEBmuypm,hW=jvPsY9:match('^(.-)(%d.*)$')
      if
      not
      (

          tEBmuypm=='<'or tEBmuypm==
              '<='or tEBmuypm==''or tEBmuypm=='='or tEBmuypm=='=='or tEBmuypm=='>='or tEBmuypm=='>'or
              tEBmuypm=='!='or
              tEBmuypm=='^'or
              tEBmuypm=='~')then return nil else return tEBmuypm,hW end end;YKDL=BMv end;local oFyb6OLp
  do local iOcgdUx
    local kCwLIk={parse=function(_l,rjQ)local Euo0={}local LIV=1;for vydlAbZ3 in rjQ:gmatch('[^,]+')do
      Euo0[LIV]=YKDL(vydlAbZ3)LIV=LIV+1 end;return Euo0 end,match=function(BXxv5z,mKLU)
      local Him=BXxv5z.specs;for cPDhu=1,#Him do local UQnOS=Him[cPDhu]
        if not UQnOS:match(mKLU)then return false end end;return true end,filter=function(tRWU,X2Zy_nb)
      local ITtw3N7E=0
      return
      function()
        while true do ITtw3N7E=ITtw3N7E+1;local yozOp=X2Zy_nb[ITtw3N7E]
          if not(yozOp)then return nil end;if tRWU:match(yozOp)then return yozOp end end end end,select=function(wxU,kOmS5sy)
      local CLSdD;do local Fh={}local IlAPA=1
        for jLKMpQuK in wxU:filter(kOmS5sy)do Fh[IlAPA]=jLKMpQuK;IlAPA=IlAPA+1 end;CLSdD=Fh end
      if#CLSdD>0 then
        local sUQpby=CLSdD[1]for mbA=1,#CLSdD do local _qPhpaFx=CLSdD[mbA]
        if sUQpby<_qPhpaFx then sUQpby=_qPhpaFx end end;return sUQpby else return nil end end,__index=function(zex,pPGcdu)if
    zex:match(pPGcdu)then return true else return nil end end,__pairs=function(rjp)return
    pairs(rjp.specs)end,__ipairs=function(cT2z)return ipairs(cT2z.specs)end,__tostring=function(zke1tWps)
      return
      fs52REi((function()
        local gRFA={}local jX9a0tJX=1;local YFy4TGc=zke1tWps.specs
        for YjpbYkCb=1,#YFy4TGc do local L1p7luJ=YFy4TGc[YjpbYkCb]
          gRFA[jX9a0tJX]=tostring(L1p7luJ)jX9a0tJX=jX9a0tJX+1 end;return gRFA end)(),',')end,__eq=function(eH,WpOZ)
      local fD2289=eH.specs
      for folfO=1,#fD2289 do local vtsK=fD2289[folfO]local E1p4Mv=false;local IHap=WpOZ.specs
        for rDvV=1,#IHap do
          local RX1L2q=IHap[rDvV]if vtsK==RX1L2q then E1p4Mv=true;break end end;if not E1p4Mv then return false end end;return true end}kCwLIk.__index=kCwLIk
    iOcgdUx=setmetatable({__init=function(bCBtWguf,q)if type(q)=='string'then q={q}end
      local e1sXUN4f
      do local x={}local VP=1;for IQwqq=1,#q do local Xcc4=q[IQwqq]x[VP]=bCBtWguf:parse(Xcc4)
        VP=VP+1 end;e1sXUN4f=x end;bCBtWguf.specs={}for fqw5=1,#e1sXUN4f do local qnVfOeRE=e1sXUN4f[fqw5]
        for YIiSKsxK=1,#qnVfOeRE do
          local Ua=qnVfOeRE[YIiSKsxK]PUNkgaiM(bCBtWguf.specs,Ua)end end end,__base=kCwLIk,__name="Spec"},{__index=kCwLIk,__call=function(qeJtG,...)
      local pdpNgBcZ=setmetatable({},kCwLIk)qeJtG.__init(pdpNgBcZ,...)return pdpNgBcZ end})kCwLIk.__class=iOcgdUx;oFyb6OLp=iOcgdUx end;local oGdh_mv
  oGdh_mv=function(wV,rLd)return aguhyl(aSdZU3(wV,aSdZU3(rLd)))end;local WjvvK;WjvvK=function(z8oF,DB6A7N)
    return oFyb6OLp(z8oF):match(aSdZU3(DB6A7N))end
  local TASVwBgU
  TASVwBgU=function(VhYX)return({aSdZU3:parse(VhYX)})[1]end
  return{Spec=oFyb6OLp,SpecItem=YKDL,Version=aSdZU3,compare=oGdh_mv,match=WjvvK,validate=TASVwBgU}end)()local pzDMZwG;pzDMZwG=require("component").isAvailable;local XPoQB,XxJ
do
  local Ha7ErH=require("shell")XPoQB,XxJ=Ha7ErH.parse,Ha7ErH.getWorkingDirectory end;local o5sms=require("shell")local JQi1jg,wVzn,pE,RSjapQ,QJf,zC
do
  local rjU95v=require("filesystem")
  JQi1jg,wVzn,pE,RSjapQ,QJf,zC=rjU95v.isDirectory,rjU95v.exists,rjU95v.makeDirectory,rjU95v.concat,rjU95v.copy,rjU95v.lastModified end;local pfZ3SPy_=require("filesystem")local pDNa2ox6,Do6yo7nm
do
  local sxBl=require("serialization")pDNa2ox6,Do6yo7nm=sxBl.serialize,sxBl.unserialize end;local y06X3k;y06X3k=require("event").pull;local ivnJjrA,d3fMjkg,el
do
  local m=require("term")ivnJjrA,d3fMjkg,el=m.clearLine,m.getCursor,m.clear end;local Wu_uIt;Wu_uIt=os.exit;local w,sgeP;do local nD4LhX6z=io
  w,sgeP=nD4LhX6z.write,nD4LhX6z.stderr end;local CM,Qlmlet;do local iN=table
  CM,Qlmlet=iN.insert,iN.unpack end;local _=pfZ3SPy_.list;local RkGFh6,hw18={},{}local nvCiFt7r=nil
local xSebv5Jc={}local mMp={}local rDtVf={}local vj="/etc/hpm/module/"local z="/var/lib/hpm/dist/"local Zg=0
local ykRppH="/etc/hpm/hpm.cfg"local WQ6=[[Usage: hpm OPTIONS COMMAND
See `man hpm` for more info.]]
local y36Aetn=[[-- << Global settings >> -------------------------------------------------------
-- A directory where package manifests will be placed.
-- It will be created if it doesn't exist.
dist = "/var/lib/hpm/dist"

-- A place where to search for custom hpm modules.
-- It will be created if it doesn't exist.
modules = "/etc/hpm/module"

-- << Settings related to the hel module >> ------------------------------------
hel = {}

-- If set to `false`, hpm will *only* remove a package that hpm is told to
-- remove. Otherwise, all of its dependants will be also removed.
hel.remove_dependants = true

-- << Settings related to the oppm module >> -----------------------------------
oppm = {}

-- A cache file where package manifests will be stored for faster access.
oppm.cache_file = "/var/cache/hpm/oppm"

-- See hel.remove_dependants above.
oppm.remove_dependants = true

-- Connect additional GitHub repositories not present in OpenPrograms' list.
-- The format is the same as in the oppm.cfg file at OpenPrograms:
-- https://github.com/OpenPrograms/openprograms.github.io/blob/master/repos.cfg
oppm.custom_repos = {
  -- ["My-Super-Repository"] = {
  --   repo="My-GitHub-Username/my-programs"
  -- }
}
]]
local iPL3B4cr={info=function(...)
  if RkGFh6.v then
    return
    print(table.concat((function(...)local Lq={}local s9tW=1;local R61K={...}for Jf4os=1,#R61K do local a4xc=R61K[Jf4os]
      Lq[s9tW]=tostring(a4xc)s9tW=s9tW+1 end;return Lq end)(...),"\t"))end end,print=function(...)
  if
  not(RkGFh6.q)then
    return
    print(table.concat((function(...)local e={}local la5=1;local i={...}for R=1,#i do local xWVu=i[R]
      e[la5]=tostring(xWVu)la5=la5+1 end;return e end)(...),"\t"))end end,error=function(...)
  if
  not(RkGFh6.q)then
    return
    sgeP:write(
        table.concat((function(...)local Yw8Yxix={}local i=1;local VoXG={...}
          for JL0I04c=1,#VoXG do
            local En6r_K97=VoXG[JL0I04c]Yw8Yxix[i]=tostring(En6r_K97)i=i+1 end;return Yw8Yxix end)(...),"\t")..'\n')end end,fatal=function(...)
  if
  not(RkGFh6.q)then
    sgeP:write(
        table.concat((function(...)local T4AA={}local VnuCKTdu=1;local XnNgn={...}
          for H1JD=1,#XnNgn do
            local gEEa9I=XnNgn[H1JD]T4AA[VnuCKTdu]=tostring(gEEa9I)VnuCKTdu=VnuCKTdu+1 end;return T4AA end)(...),"\t")..'\n')end;return Wu_uIt(1)end}local GI2hz6SK
GI2hz6SK=function(ULLLDUm,e4F3)
  if not(ULLLDUm)then return iPL3B4cr.fatal(e4F3)end end;local Oh
Oh=function(GsfNt7)return
iPL3B4cr.fatal((tostring(GsfNt7))..": Not implemented yet!")end;local PG;PG=function()w(WQ6)return Wu_uIt(0)end;local n
n=function(fF0,YWPfQKb2)if
not(fF0)then iPL3B4cr.fatal(YWPfQKb2)end;return fF0 end;local O
O=function(r,OS0Zp3i,BK)
  if not(type(r==OS0Zp3i))then
    iPL3B4cr.fatal("Value '"..
        tostring(r).."' is "..

        tostring(type(BK))..", however, a "..tostring(OS0Zp3i).." is expected.")end;return BK end;local N5UjTN
N5UjTN=function(Idjbe70)return O(Idjbe70,"number",tonumber(Idjbe70))end;local qLH5
qLH5=function(B)return O(B,"string",tostring(B))end;local tE
tE=function(nDjt,NVWt)
  for efuUGMh,p4nNp in pairs(NVWt)do if p4nNp==nDjt then return true,efuUGMh end end;return false end;local VcV0EuD
VcV0EuD=function(VW)local Zt=0;for V,mzeTI in pairs(VW)do Zt=Zt+1 end;return Zt end;local pX4gCR
pX4gCR=function(s)
  if type(s)=="nil"then return true elseif type(s)=="string"then return not s or#s<1 elseif
  type(s)=="table"then return not s or VcV0EuD(s)<1 else return true end end;local gad4ZcL
gad4ZcL=function(y4J)
  for ztJhP_u8=1,#y4J do local D=y4J[ztJhP_u8]if not D then return false end end;return true end;local dk
dk=function(XIcl)return wVzn(XIcl)and JQi1jg(XIcl)end;local E
E=function(ys)return wVzn(ys)and not JQi1jg(ys)end;local OO
OO=function(rMQ1um8)return rMQ1um8 ==1 and""or"s"end;local y
y=function(U2)return U2 ~=1 and""or"s"end;local cR6rJlAl
cR6rJlAl=function(X)return X==1 and"is"or"are"end;local M6ilzGJ
M6ilzGJ=function(zLtWO09,Z)
  for ZDICnKE,L in pairs(Z)do if type(L)~="table"or
      type(zLtWO09[ZDICnKE])~="table"then zLtWO09[ZDICnKE]=L else
    M6ilzGJ(zLtWO09[ZDICnKE],L)end end end;local iW6CD
iW6CD=function(B58)if type(B58)~="table"then return B58 end;local PYVzrNl={}for KTVmRC,Pa in pairs(B58)do
  PYVzrNl[KTVmRC]=iW6CD(Pa)end;return PYVzrNl end;local wZdg
wZdg=function()do local OJPc3R=io.open("/tmp/hpm-time","w")
  OJPc3R:write("")OJPc3R:close()end
  local bmK=zC("/tmp/hpm-time")pfZ3SPy_.remove("/tmp/hpm-time")return bmK end;local BaX
BaX=function(j)
  if
  pfZ3SPy_.get(o5sms.resolve(j)).isReadOnly()then return false,"the path is readonly!"elseif not wVzn(j)then
    return false,"the filesystem node doesn't exist."else
    if
    not(JQi1jg(j)or pfZ3SPy_.isLink(j))then return pfZ3SPy_.remove(j)else for vMgKnGj in n(_(j))do
      BaX(RSjapQ(j,vMgKnGj))end;return pfZ3SPy_.remove(j)end end end;local SJsW11k
SJsW11k=function()
  local M9K=RkGFh6.c or RkGFh6.config or ykRppH
  if not E(M9K)then local W0iTcMIt=pfZ3SPy_.path(M9K)
    if not dk(W0iTcMIt)then
      local Dq,y3Ur=pE(W0iTcMIt)
      if not Dq then return false,
      "Failed to create '"..tostring(W0iTcMIt)..
          "' directory for the config file: "..tostring(y3Ur)end end;local N,Hald6SO=io.open(M9K,"w")if N then N:write(y36Aetn)N:close()else
    return false,
    "Failed to open config file for writing: "..tostring(Hald6SO)end end;local Zeu,Q2_d=io.open(M9K,"r")
  if Zeu then local GL70F7uL=Zeu:read("*all")
    Zeu:close()local lqANrrJA={}
  (load(GL70F7uL,"config","t",lqANrrJA))()local WUFTXBy6
    WUFTXBy6=function(aEZf)if aEZf==nil then aEZf={}end
      return
      setmetatable(aEZf,{__index={get=function(QjQ_o,wDiq_,QYA5WJOY)
        if
        type(aEZf[QjQ_o])~="nil"then if type(aEZf[QjQ_o])=="table"then
          return WUFTXBy6(aEZf[QjQ_o])end;return aEZf[QjQ_o]end
        iPL3B4cr.error("Attempt to access undeclared config field '"..tostring(QjQ_o).."'!")
        if not QYA5WJOY then return wDiq_ else return WUFTXBy6(wDiq_)end end}})end;mMp=WUFTXBy6(lqANrrJA)vj=mMp.get("modules",vj)
    z=mMp.get("dist",z)return mMp else
    return false,"Failed to open config file for reading: "..tostring(Q2_d)end end;local Ki1HJT
Ki1HJT=function()if not(pzDMZwG("internet"))then
  iPL3B4cr.fatal("This command requires an internet card to run!")end;nvCiFt7r=nvCiFt7r or
    require("internet").request end;local wjim8xCV
wjim8xCV=function(yliV8)Ki1HJT()return pcall(nvCiFt7r,yliV8)end;local EQLam
EQLam=function()
  if not dk(vj)then local YUGQovw,XZt7GyF=pE(vj)if not YUGQovw then
    return false,"Failed to create '"..
        tostring(vj)..
        "' directory for custom modules: "..tostring(XZt7GyF)end end;local rjpKFl=n(_(vj))
  for Zn3SC in rjpKFl do rDtVf.name=Zn3SC:match("^(.+)%..+$")
    local D4=n(loadfile(RSjapQ(vj,Zn3SC),"t",rDtVf))D4()rDtVf.name=nil end;return true end;local qTDt
qTDt=function(crA9EKx)local IcsJ=crA9EKx;local A
  do local Wp9xT=crA9EKx:find(':')if Wp9xT then
    IcsJ=crA9EKx:sub(Wp9xT+1)A=crA9EKx:sub(1,Wp9xT-1)end end
  if not A then local P={}
    for o0_XG8FI,A in pairs(xSebv5Jc)do
      if A[IcsJ]then if type(A[IcsJ])=="table"and
          A[IcsJ].__public==true then
        CM(P,{class=A,module=o0_XG8FI,method=A[IcsJ]})end end end
    if#P>1 then local jLsxpw=nil
      for x,A in pairs(P)do if A.module=="hel"then jLsxpw=x;break end end;if jLsxpw then P={P[jLsxpw]}end end
    if#P>1 then
      iPL3B4cr.print("Ambiguous choice: method "..
          tostring(IcsJ).." is implemented in the following modules:")for AXNfV=1,#P do local A=P[AXNfV]
      iPL3B4cr.print(" * "..tostring(A.module))end
      iPL3B4cr.print(
          "Choose a specific module by prepending its name with a colon, e.g., "..
              tostring(P[1].module)..":"..tostring(IcsJ)..".")return false elseif#P==0 then
      iPL3B4cr.error("Unknown command: "..tostring(IcsJ))return false else A=P[1].module
      iPL3B4cr.info("Note, using "..tostring(A)..":"..
          tostring(IcsJ)..".")
      return function(...)return P[1].method(P[1].class,...)end end else
    if xSebv5Jc[A]and pX4gCR(IcsJ)then local cX={}
      for iyx,bxvn in pairs(xSebv5Jc[A])do if
      type(bxvn)=="table"and bxvn.__public==true then
        CM(cX,tostring(iyx))end end
      iPL3B4cr.print("Available module-specific commands: "..tostring(table.concat(cX,", ")))return false end
    if
    not xSebv5Jc[A]or not xSebv5Jc[A][IcsJ]or
        xSebv5Jc[A][IcsJ]and
            (type(xSebv5Jc[A][IcsJ])~="table"or
                xSebv5Jc[A][IcsJ].__public~=true)then
      iPL3B4cr.error("Unknown command: "..tostring(A)..":"..tostring(IcsJ))return false else return function(...)
      return xSebv5Jc[A][IcsJ](xSebv5Jc[A],...)end end end end;local v
v=function(mWYrzB)
  if not mWYrzB or mWYrzB==""then mWYrzB="hel"else mWYrzB=mWYrzB end;return xSebv5Jc[mWYrzB]or xSebv5Jc.default end;local Ta
Ta=function(O7kX,Q4XSpdY,...)if O7kX==nil then O7kX=xSebv5Jc.default end
  if O7kX[Q4XSpdY]then return
  O7kX[Q4XSpdY](O7kX,...)else return
  xSebv5Jc.default[Q4XSpdY](xSebv5Jc.default,...)end end;local unArcvQl
unArcvQl=function(fzTyrQ9F,fAumJ0i,i0,tZliF4)if fAumJ0i==nil then fAumJ0i="hel"end;if i0 ==nil then
  i0=RSjapQ(z,fAumJ0i)end
  if tZliF4 ==nil then tZliF4=fzTyrQ9F.name end;if not fzTyrQ9F then return false,"'nil' given"end
  if not dk(i0)then
    local uS_N6,o5SLRA=pE(i0)
    if not uS_N6 then return false,
    "Failed to create '"..tostring(RSjapQ(i0,fAumJ0i)).."' directory for manifest files: "..
        tostring(o5SLRA)end end;local jlmopoj,R=io.open(RSjapQ(i0,tZliF4),"w")
  if jlmopoj then
    jlmopoj:write(pDNa2ox6(fzTyrQ9F))jlmopoj:close()return true else return false,
  "Failed to open file for writing: "..tostring(R)end end;local h6Ub7U
h6Ub7U=function(ztwXaCR,M2WtMgiq,FgfME)if FgfME==nil then FgfME="hel"end
  M2WtMgiq=M2WtMgiq or RSjapQ(z,FgfME,ztwXaCR)
  if E(M2WtMgiq)then local ylH9o,CC4Kfjh=io.open(M2WtMgiq,"rb")
    if ylH9o then
      local k=n(Do6yo7nm(ylH9o:read("*all")))ylH9o:close()return k else return false,
    "Failed to open manifest for '"..tostring(ztwXaCR).."' package: "..
        tostring(CC4Kfjh)end else return false,
  "No manifest found for '"..tostring(ztwXaCR).."' package"end end;local Gm
Gm=function(eUQ0x,r0OR)if r0OR==nil then r0OR="hel"end;local pYHkv=RSjapQ(z,r0OR,eUQ0x)
  if
  E(pYHkv)then return BaX(pYHkv)else return false,
  "No manifest found for '"..tostring(eUQ0x).."' package"end end;local YKA7cU
YKA7cU=function(hxZHlgP)return
setmetatable({__public=true},{__call=function(zct,...)return hxZHlgP(...)end})end;local mCsewfX
mCsewfX=function(WQk6Wkd,t)return
function()local pRCHPl,sCffg4HK=pcall(WQk6Wkd)if not(pRCHPl)then
  return false,tostring(sCffg4HK)else return sCffg4HK end end end;local yY
yY=function(EyljhkFp,uGDn542,DQ)
  if uGDn542 ==nil then uGDn542="Could not download '%s': %s"end;if DQ==nil then DQ="Could not download '%s': %s"end
  local s6Ahlni_,T6dNu,H=wjim8xCV(EyljhkFp)if not(s6Ahlni_ and T6dNu)then
    return false,uGDn542:format(EyljhkFp,H)end;local YlzZm=""for vj9879b5,H in mCsewfX(T6dNu)do
    if vj9879b5 then
      YlzZm=YlzZm..vj9879b5 else return false,DQ:format(EyljhkFp,H)end end;return YlzZm end;local Xf
Xf=function()
  if not(RkGFh6.y)then
    io.write("Press [ENTER] to continue...")local cotcYZ1f=select(3,y06X3k("key_down"))
    if cotcYZ1f==13 then
      ivnJjrA()return true else io.write("\n")return false end else return true end end;local UlFdiZ7v
UlFdiZ7v=function(FRcmT)local zfl=0;local itxD={}
  if not(pX4gCR(FRcmT.install))then
    local JPHs7A={"Packages to INSTALL:",table.concat(FRcmT.install,"  ")}CM(itxD,JPHs7A)zfl=zfl+#FRcmT.install else
    FRcmT.install={}end
  if not(pX4gCR(FRcmT.reinstall))then
    local yzYgnMtr={"Packages to REINSTALL:",table.concat(FRcmT.reinstall,"  ")}CM(itxD,yzYgnMtr)zfl=zfl+#FRcmT.reinstall else
    FRcmT.reinstall={}end
  if not(pX4gCR(FRcmT.upgrade))then
    local o={"Packages to UPGRADE:",table.concat(FRcmT.upgrade,"  ")}CM(itxD,o)zfl=zfl+#FRcmT.upgrade else FRcmT.upgrade={}end
  if not(pX4gCR(FRcmT.remove))then
    local wmkJ={"Packages to REMOVE:",table.concat(FRcmT.remove,"  ")}CM(itxD,wmkJ)zfl=zfl+#FRcmT.remove else FRcmT.remove={}end
  do
    local I1={tostring(#FRcmT.install).." to INSTALL, "..
                  tostring(#FRcmT.reinstall)..
                  " to REINSTALL, "..
                  tostring(#FRcmT.upgrade).." to UPGRADE, "..tostring(#FRcmT.remove)..
                  " to REMOVE."}CM(itxD,I1)end
  for gXu5hG,R60Ru4bj in pairs(itxD)do for gXu5hG,eQWRf in pairs(R60Ru4bj)do
    if gXu5hG==1 then iPL3B4cr.print(eQWRf)else iPL3B4cr.print(
        "  "..tostring(eQWRf))end end;if
  gXu5hG~=#itxD then iPL3B4cr.print("")end end
  if zfl>0 then if not(Xf())then return Wu_uIt(7)end end end
do local WT2AX;local _AvO={}_AvO.__index=_AvO
  WT2AX=setmetatable({__init=function()end,__base=_AvO,__name="default"},{__index=_AvO,__call=function(q,...)
    local WUY7=setmetatable({},_AvO)q.__init(WUY7,...)return WUY7 end})_AvO.__class=WT2AX;local qEO=WT2AX
  qEO.install=function()return
  iPL3B4cr.fatal("Incorrect source is provided! No default 'install' implementation.")end
  qEO.remove=function(qEO,_puepou,DYLeJ)if DYLeJ==nil then DYLeJ="hel"end
    if _puepou then
      if _puepou.files then
        for udbF,dt1 in
        pairs(_puepou.files)do
          if dt1.path then local V7eMEiVW,Co1tUVas=BaX(dt1.path)if not(V7eMEiVW)then
            iPL3B4cr:error("Failed to remove '"..
                tostring(dt1.path).."': "..tostring(Co1tUVas))end else
            local B,UjlBMb=BaX(RSjapQ(dt1.dir,dt1.name))if not(B)then
            iPL3B4cr:error("Failed to remove '"..tostring(RSjapQ(dt1.dir,dt1.name)).."': "..
                tostring(UjlBMb))end end end end;return Gm(_puepou.name,DYLeJ)else
      return false,"Package can't be removed: the manifest is empty."end end;xSebv5Jc.default=WT2AX end
do local PKWIJ9;local rQYWEt=xSebv5Jc.default;local nCwsa={}nCwsa.__index=nCwsa
  setmetatable(nCwsa,rQYWEt.__base)
  PKWIJ9=setmetatable({__init=function(zYGA2q2,...)return PKWIJ9.__parent.__init(zYGA2q2,...)end,__base=nCwsa,__name="hel",__parent=rQYWEt},{__index=function(I9Mw,e)
    local BUtIET=rawget(nCwsa,e)if BUtIET==nil then local NvAj=rawget(I9Mw,"__parent")
      if NvAj then return NvAj[e]end else return BUtIET end end,__call=function(Icg,...)
    local PzMsk=setmetatable({},nCwsa)Icg.__init(PzMsk,...)return PzMsk end})nCwsa.__class=PKWIJ9;local IPPy=PKWIJ9;IPPy.URL="https://api.fomalhaut.me/"
  IPPy.parsePackageJSON=function(IPPy,axLuO,j)if
  j==nil then j=u.Spec("*")end;local As=nil;local JmCzKm={}
    for PXrrrSid,L9 in
    pairs(axLuO.versions)do local _KZPScl=u.Version(PXrrrSid)if not(_KZPScl)then
      iPL3B4cr.fatal(
          "Could not parse the version in package: "..tostring(_KZPScl))end;JmCzKm[_KZPScl]=L9 end
    local Mwhc,A6z=pcall(function()
      return
      j:select((function()local dbTwy={}local R4f819q=1;for Kj1I,nTUMgqomA in pairs(JmCzKm)do dbTwy[R4f819q]=Kj1I
        R4f819q=R4f819q+1 end;return dbTwy end)())end)if not(Mwhc)then
      iPL3B4cr.fatal("Could not select the best version: "..tostring(A6z))end;As=tostring(A6z)if not(A6z)then
      iPL3B4cr.fatal(
          "No candidate for version specification '"..tostring(j).."' found!")end
    local _Mk={name=axLuO.name,version=As,files={},dependencies={}}for Id5sIM,gZM2ANLt in pairs(JmCzKm[A6z].files)do local aC72qEnu=gZM2ANLt.path
      CM(_Mk.files,{url=Id5sIM,path=aC72qEnu})end;for B60J,Y4 in
    pairs(JmCzKm[A6z].depends)do local f=Y4.version;local yeCnvcd6=Y4.type
      CM(_Mk.dependencies,{name=B60J,version=f,type=yeCnvcd6})end
    return _Mk end
  IPPy.getPackageSpec=function(IPPy,Iq93c6cA)
    iPL3B4cr.info("Downloading package data for "..tostring(Iq93c6cA).." ...")
    local nsM0h,Czi=wjim8xCV(IPPy.URL.."packages/"..Iq93c6cA)if not(nsM0h)then
      iPL3B4cr.fatal("HTTP request error: "..Czi)end;local IlxN=""
    local EA_3x01A,m54tY2=xpcall(function()
      for AhbP in Czi do IlxN=IlxN..AhbP end end,debug.traceback)
    if not(EA_3x01A)then
      local QHFgYUN={"HTTP request error.","Perhaps the package "..tostring(Iq93c6cA)..
          " doesn't exist, or the Hel Repository went down.","Rerun with -v to see traceback.",
                     "\nError details:\n"..tostring(m54tY2)}if RkGFh6.v then table.remove(QHFgYUN,3)else
      table.remove(QHFgYUN,4)end
      iPL3B4cr.fatal(table.concat(QHFgYUN,"\n"))end;local WJWMdKI=JD:decode(IlxN)if not(WJWMdKI)then
      iPL3B4cr.fatal("Incorrect JSON format!\n"..
          tostring(IlxN))end;return WJWMdKI.data end
  IPPy.rawInstall=function(IPPy,RoEsr7So,dX,Rz)if dX==nil then dX=false end;if Rz==nil then Rz=false end;local j177r;if Rz then
    j177r=RSjapQ(XxJ(),RoEsr7So.name)else j177r="/"end
    if Rz and not dk(j177r)then
      local j,qCaFw=pE(j177r)
      if not(j)then
        iPL3B4cr.fatal("Failed creating '"..tostring(j177r)..
            "' directory for package '"..
            tostring(RoEsr7So.name).."'! \n"..tostring(qCaFw))end elseif not Rz then local syvPi=h6Ub7U(RoEsr7So.name,nil,"hel")
      if syvPi then
        if syvPi.version==
            tostring(RoEsr7So.version)then
          iPL3B4cr.print("'"..tostring(RoEsr7So.name)..
              "@"..
              tostring(syvPi.version).."' is already installed, skipping...")return syvPi else
          iPL3B4cr.fatal("'"..
              tostring(RoEsr7So.name)..
              "@"..

              tostring(RoEsr7So.version)..
              "' was attempted to install, however, another version of the same package is already installed: '"..
              tostring(RoEsr7So.name)..
              "@"..tostring(syvPi.version).."'")end end end
    for NrgSK2,wIH in pairs(RoEsr7So.files)do
      iPL3B4cr.info("Fetching '"..
          tostring(pfZ3SPy_.name(wIH.path)).."' ...")local TYWkpc=n(yY(wIH.url))
      local k=RSjapQ(j177r,pfZ3SPy_.path(wIH.path))
      if not dk(k)then local J,gtlO9=pE(k)if not(J)then
        iPL3B4cr.fatal("Failed to create '"..tostring(k)..
            "' directory for '"..
            tostring(pfZ3SPy_.name(wIH.path)).."'! \n"..tostring(gtlO9))end end
      do local Lun
        wIH,Lun=io.open(RSjapQ(k,pfZ3SPy_.name(wIH.path)),"w")if not(wIH)then
        iPL3B4cr.fatal("Could not open '"..

            tostring(RSjapQ(k,pfZ3SPy_.name(wIH.path))).."' for writing: "..tostring(Lun))end
        wIH:write(TYWkpc)wIH:close()end end;return
    {name=RoEsr7So.name,version=tostring(RoEsr7So.version),files=RoEsr7So.files,dependencies=RoEsr7So.dependencies,manual=dX}end
  IPPy.resolveDependencies=function(IPPy,beUJXhjw,zY7adu,Nlvw,K55,BJcMTdMi)if Nlvw==nil then Nlvw={}end;if K55 ==nil then K55={}end;if
  BJcMTdMi==nil then BJcMTdMi={}end
    for f1MKKJ=1,#beUJXhjw do local nFf=beUJXhjw[f1MKKJ]local EIqL41,iv
      EIqL41,iv=nFf.name,nFf.version;local rfmMR4=false;for Tq2I=1,#Nlvw do local GNo=Nlvw[Tq2I]
      if GNo.pkg.name==EIqL41 then rfmMR4=true;break end end
      if not(rfmMR4)then
        CM(K55,{name=EIqL41,version=""})local e5x=h6Ub7U(EIqL41,nil,"hel")
        if not e5x or not
        iv:match(u.Version(e5x.version))then
          local QrONvWGq=IPPy:getPackageSpec(EIqL41)local D94fnZaa=IPPy:parsePackageJSON(QrONvWGq,iv)
          K55[#K55].version=D94fnZaa.version;local XI=D94fnZaa.dependencies
          for FNi=1,#XI do local pRW2nEmK=XI[FNi]rfmMR4=false;for OR=1,#Nlvw do
            local Arww=Nlvw[OR]
            if Arww.pkg.name==pRW2nEmK.name then rfmMR4=true;break end end
            if not rfmMR4 then local BYH=nil
              for o7E8TLH,N5N27Jd in pairs(K55)do if
              N5N27Jd.name==pRW2nEmK.name then BYH=o7E8TLH;break end end
              if BYH then
                if K55[BYH].version==pRW2nEmK.version then
                  iPL3B4cr.fatal("Circular dependencies detected: '"..

                      tostring(EIqL41)..
                      "@"..
                      tostring(D94fnZaa.version)..
                      "' depends on '"..
                      tostring(pRW2nEmK.name)..
                      "@"..

                      tostring(pRW2nEmK.version).."', and '"..
                      tostring(K55[BYH].name).."@"..
                      tostring(K55[BYH].version)..
                      "' depends on '"..tostring(EIqL41)..
                      "@"..tostring(D94fnZaa.version).."'.")else
                  iPL3B4cr.fatal("Attempted to install two versions of the same package: '"..
                      tostring(pRW2nEmK.name)..
                      "@"..

                      tostring(pRW2nEmK.version)..
                      "' and '"..
                      tostring(K55[BYH].name)..
                      "@"..tostring(K55[BYH].version)..

                      "' when resolving dependencies for '"..tostring(EIqL41)..
                      "@"..tostring(D94fnZaa.version).."'.")end end
              IPPy:resolveDependencies({{name=pRW2nEmK.name,version=u.Spec(pRW2nEmK.version)}},false,Nlvw,K55,BJcMTdMi)end end;CM(Nlvw,{pkg=D94fnZaa})
          CM(BJcMTdMi,{pkg=D94fnZaa})else CM(Nlvw,{pkg=e5x})
          if zY7adu then CM(BJcMTdMi,{pkg=e5x})end end;K55[#K55]=nil end end;return BJcMTdMi end
  IPPy.getPackageDependants=function(IPPy,m,nK,_zr)if nK==nil then nK={}end;if _zr==nil then _zr={}end
    for f5=1,#m do
      local UAc=m[f5]local Ef=false
      for P=1,#nK do local F4AWvI=nK[P]if F4AWvI.name==UAc then Ef=true;break end end
      if not(Ef)then CM(_zr,{name=UAc})local GYVN=h6Ub7U(UAc,nil,"hel")
        if GYVN then
          CM(nK,{name=UAc,manifest=GYVN})local DNlB1V=n(_(RSjapQ(z,"hel")))
          for erb6G_E in DNlB1V do
            GYVN=n(h6Ub7U(erb6G_E,nil,"hel"))local QFUU10K=GYVN.dependencies
            for xNPDtul=1,#QFUU10K do local k8=QFUU10K[xNPDtul]
              if k8.name==UAc then
                Ef=false;for HmgRk=1,#nK do local UuCdpVi=nK[HmgRk]
                if UuCdpVi.name==erb6G_E then Ef=true;break end end
                if not Ef then
                  for fghe=1,#_zr do local vFXf=_zr[fghe]if
                  vFXf.name==erb6G_E then
                    iPL3B4cr.fatal("Circular dependencies detected: "..tostring(erb6G_E))end end;IPPy:getPackageDependants({erb6G_E},nK,_zr)end end end end else
          iPL3B4cr.fatal("Package "..
              tostring(UAc).." is referenced as a dependant of another package, however, this package isn't installed.")end;_zr[#_zr]=nil end end;return nK end
  IPPy.fileConflicts=function(IPPy,CA0uX7n)if RkGFh6.f or RkGFh6.force then
    iPL3B4cr.info("File conflict checking skipped: --force option given.")return true end
    iPL3B4cr.info("Checking for file conflicts...")local ze5Vpc3=false
    for vwK8=1,#CA0uX7n do local Sk_SiC=CA0uX7n[vwK8]local X0bgPvA=Sk_SiC.files
      for M9CyqH=1,#X0bgPvA do
        local z0x4qSAN=X0bgPvA[M9CyqH]
        if wVzn(z0x4qSAN.path)then
          iPL3B4cr.error("'"..
              tostring(Sk_SiC.name).."' wants to override node at '"..
              tostring(z0x4qSAN.path).."'")ze5Vpc3=true end end end;if ze5Vpc3 then
      iPL3B4cr.fatal("File conflicts detected; terminating.")end;return true end
  IPPy.install=YKA7cU(function(IPPy,...)
    if RkGFh6.l or RkGFh6["local"]then
      local S=o5sms.resolve(...)
      local NKetZhs=n(h6Ub7U(S,RSjapQ(S,"manifest")))
      local EFLZ0N1=IPPy:resolveDependencies((function()local rNOL8G={}local q=1;local lKO=NKetZhs.dependencies
        for hcwgu=1,#lKO do
          local omgCdqp8=lKO[hcwgu]local X17eHTx,SGF;X17eHTx,SGF=omgCdqp8.name,omgCdqp8.version
          rNOL8G[q]={name=X17eHTx,version=u.Spec(SGF)}q=q+1 end;return rNOL8G end)())local gL=RkGFh6.d or RkGFh6.onlyDeps;local m4={}for myIHU=1,#EFLZ0N1 do
      local xxNCdF=EFLZ0N1[myIHU]
      CM(m4,tostring(xxNCdF.pkg.name)..
          "@"..tostring(xxNCdF.pkg.version))end;if not(gL)then
      CM(m4,
          tostring(NKetZhs.name).."@"..tostring(NKetZhs.version))end;UlFdiZ7v({install=m4})
      IPPy:fileConflicts((function()
        local _cl1b={}local Xz18nk=1;for P=1,#EFLZ0N1 do local sTX4=EFLZ0N1[P]_cl1b[Xz18nk]=sTX4.pkg
          Xz18nk=Xz18nk+1 end;return _cl1b end)())
      for A0TJx=1,#EFLZ0N1,1 do local Nqdkw=EFLZ0N1[A0TJx]
        iPL3B4cr.print("Installing '"..

            tostring(Nqdkw.pkg.name).."@"..tostring(Nqdkw.pkg.version).."'...")local t=IPPy:rawInstall(Nqdkw.pkg,false,false)
        local QbMO,wYZ=unArcvQl(t,"hel")
        if QbMO then
          iPL3B4cr.info("Saved the manifest of '"..tostring(t.name).."'.")else
          iPL3B4cr.fatal("Couldn't save the manifest of '"..tostring(t.name)..
              "': "..tostring(wYZ)..".")end end
      if not gL then
        iPL3B4cr.print("Installing '"..tostring(NKetZhs.name).."@"..
            tostring(NKetZhs.version).."'...")
        for tx1LD,N3ROeR in pairs(NKetZhs.files)do
          local I1oQVnUd=N3ROeR.path or RSjapQ(N3ROeR.dir,N3ROeR.name)if not dk(pfZ3SPy_.path(I1oQVnUd))then
          pE(pfZ3SPy_.path(I1oQVnUd))end
          local oTX,WZlF4=QJf(RSjapQ(S,N3ROeR.url),I1oQVnUd)if not(oTX)then
          iPL3B4cr.fatal("Cannot copy file '"..tostring(N3ROeR.name)..
              "': "..tostring(WZlF4))end end;NKetZhs["local"]=true;local aMd,o0pf=unArcvQl(NKetZhs,"hel")
        if aMd then
          iPL3B4cr.info(
              "Saved the manifest of '"..tostring(NKetZhs.name).."'.")else
          iPL3B4cr.fatal("Couldn't save the manifest of '"..tostring(NKetZhs.name).."': "..
              tostring(o0pf)..".")end end;iPL3B4cr.print("Done.")return true end;local X0GTupeV={}local rQ={...}
    for IxqPDOWH=1,#rQ do local GZqV=rQ[IxqPDOWH]
      local OVubrDw_,G2_TeR8=GZqV:match("^(.+)@(.+)$")or GZqV;if pX4gCR(G2_TeR8)then G2_TeR8="*"end
      iPL3B4cr.info("Creating version specification for "..
          tostring(G2_TeR8).." ...")
      local yk,OPSPMfr_=pcall(function()return u.Spec(G2_TeR8)end)if not(yk)then
      iPL3B4cr.fatal("Could not parse the version specification: "..tostring(OPSPMfr_).."!")end
      CM(X0GTupeV,{name=OVubrDw_,version=OPSPMfr_})end;local k=RkGFh6.r or RkGFh6.reinstall
    local Oc=RkGFh6.s or RkGFh6.save;local IHovU=IPPy:resolveDependencies(X0GTupeV,k)local e_wDQjk={}
    local ClglY={}
    for QnNOl=1,#IHovU do local aQs=false
      repeat local uow_0tb=IHovU[QnNOl]
        if k then local tykg=false;for C_pPyW=1,#X0GTupeV do
          local mgb4b=X0GTupeV[C_pPyW]
          if mgb4b.name==uow_0tb.pkg.name then tykg=true;break end end;if tykg then
          CM(e_wDQjk,
              tostring(uow_0tb.pkg.name).."@"..tostring(uow_0tb.pkg.version))aQs=true;break end end
        CM(ClglY,tostring(uow_0tb.pkg.name).."@"..
            tostring(uow_0tb.pkg.version))aQs=true until true;if not aQs then break end end;UlFdiZ7v({install=ClglY,reinstall=e_wDQjk})
    IPPy:fileConflicts((function()
      local LOBqxO={}local m8=1
      for mcoAHO=1,#IHovU do local d3gFWO=IHovU[mcoAHO]
        if not
        tE(tostring(d3gFWO.pkg.name).."@"..
            tostring(d3gFWO.pkg.version),e_wDQjk)then LOBqxO[m8]=d3gFWO.pkg;m8=m8+1 end end;return LOBqxO end)())
    if k then local D
      do local obodPKnu={}local kgdzk=1
        for oVSp=1,#X0GTupeV do local uBJ=X0GTupeV[oVSp]obodPKnu[kgdzk]=n(h6Ub7U(uBJ.name,
            nil,"hel"))kgdzk=kgdzk+1 end;D=obodPKnu end;IPPy:_remove(D,true,false)end
    for A=1,#IHovU do local MP=IHovU[A]
      iPL3B4cr.print("Installing '"..tostring(MP.pkg.name).."@"..
          tostring(MP.pkg.version).."'...")local jb=false
      for PPNahh=1,#X0GTupeV do local z2g=X0GTupeV[PPNahh]if z2g.name==MP.pkg.name then
        jb=true;break end end;local uKSj=IPPy:rawInstall(MP.pkg,jb,Oc)
      local YXgXQB,bvL1X4=unArcvQl(uKSj,"hel")
      if YXgXQB then
        iPL3B4cr.info("Saved the manifest of '"..tostring(uKSj.name).."'.")else
        iPL3B4cr.fatal("Couldn't save the manifest of '"..tostring(uKSj.name).."': "..
            tostring(bvL1X4)..".")end end;return iPL3B4cr.print("Done.")end)
  IPPy.remove=YKA7cU(function(IPPy,...)local m9JTkVv6={...}local Q={}for bWkP=1,#m9JTkVv6 do local JtFj=m9JTkVv6[bWkP]
    local PQ3=n(h6Ub7U(JtFj,nil,"hel"))CM(Q,PQ3)end
    IPPy:_remove(Q,false)return iPL3B4cr.print("Done.")end)
  IPPy._remove=function(IPPy,_xCtN,JVpe,nG36XmZC)if JVpe==nil then JVpe=false end
    if nG36XmZC==nil then nG36XmZC=true end;local Vf26
    if not
    mMp.get("hel",{},true).get("remove_dependants",true)or not nG36XmZC then
      do
        local xUGt={}local _U=1;for hkI39=1,#_xCtN do local MwwN=_xCtN[hkI39]
        xUGt[_U]={name=MwwN.name,manifest=MwwN}_U=_U+1 end;Vf26=xUGt end else
      Vf26=IPPy:getPackageDependants((function()local oZ9={}local OXlT0=1;for V=1,#_xCtN do local zIYNIXy1=_xCtN[V]
        oZ9[OXlT0]=zIYNIXy1.name;OXlT0=OXlT0+1 end;return oZ9 end)())end
    if not(JVpe)then
      UlFdiZ7v({remove=(function()local c={}local mReHt4h=1
        for I7=1,#Vf26 do local Upw=Vf26[I7]
          c[mReHt4h]="hel:"..

              tostring(Upw.manifest.name).."@"..tostring(Upw.manifest.version)mReHt4h=mReHt4h+1 end;return c end)()})end
    for nqBfKL=1,#Vf26 do local gs3a=Vf26[nqBfKL]
      iPL3B4cr.print("Removing '"..
          tostring(gs3a.manifest.name)..
          "@"..tostring(gs3a.manifest.version).."' ...")
      n(PKWIJ9.__parent.remove(IPPy,gs3a.manifest,"hel"))end;return true end
  IPPy.upgrade=YKA7cU(function(IPPy)local AkKaBC={}
    for mZPe4w in n(_(RSjapQ(z,"hel")))do
      if not
      (JQi1jg(RSjapQ(z,"hel",mZPe4w)))then local OvZ=n(h6Ub7U(mZPe4w,nil,"hel"))if not
      (OvZ["local"])then CM(AkKaBC,OvZ)end end end;local OmRH8={}
    for cBOpf=1,#AkKaBC do local KZYA5y=AkKaBC[cBOpf]
      local YoCAN7OU,FoP=pcall(IPPy.getPackageSpec,IPPy,KZYA5y.name)
      if YoCAN7OU then local jqtWXY=IPPy:parsePackageJSON(FoP)
        KZYA5y.latest={spec=FoP,data=jqtWXY}
        if u.Version(KZYA5y.latest.data.version)>
            u.Version(KZYA5y.version)then CM(OmRH8,KZYA5y)end end end
    local GY=IPPy:resolveDependencies((function()local XgRb={}local G3e=1;for GoP6=1,#OmRH8 do local cZ_=OmRH8[GoP6]
      XgRb[G3e]={name=cZ_.name,version=u.Spec(cZ_.latest.data.version)}G3e=G3e+1 end
      return XgRb end)())local oukM79R
    do local NYc8={}local Dff8=1
      for lEYwsOG9=1,#OmRH8 do local M=OmRH8[lEYwsOG9]
        NYc8[Dff8]=
        tostring(M.name)..
            "@{"..tostring(M.version).." => "..
            tostring(M.latest.data.version).."}"Dff8=Dff8+1 end;oukM79R=NYc8 end;local D_j={}
    for Vt95q2G=1,#GY do local jsPbwU=GY[Vt95q2G]local Wvs3rd6o=true
      for UdVlP=1,#OmRH8 do local N=OmRH8[UdVlP]if N.name==
          jsPbwU.pkg.name then Wvs3rd6o=false;break end end;if Wvs3rd6o then
      CM(D_j,tostring(jsPbwU.pkg.name)..
          "@"..tostring(jsPbwU.pkg.version))end end;UlFdiZ7v({upgrade=oukM79R,install=D_j})
    for v9mB_RUi=1,#GY do
      local hX=GY[v9mB_RUi]local AVU=false;local I=false
      for l6xUetCb=1,#OmRH8 do local lOb_Sv=OmRH8[l6xUetCb]if
      lOb_Sv.name==hX.pkg.name then AVU=lOb_Sv;I=lOb_Sv.manual;break end end;if AVU then IPPy:_remove({AVU},true,false)end
      iPL3B4cr.print(
          "Installing '"..tostring(hX.pkg.name)..
              "@"..tostring(hX.pkg.version).."'...")local _x5O1=IPPy:rawInstall(hX.pkg,I,false)
      local eFI8dI3,i=unArcvQl(_x5O1,"hel")
      if eFI8dI3 then
        iPL3B4cr.info("Saved the manifest of '"..tostring(_x5O1.name).."'.")else
        iPL3B4cr.fatal("Couldn't save the manifest of '"..tostring(_x5O1.name)..
            "': "..tostring(i)..".")end end;return iPL3B4cr.print("Done.")end)
  IPPy.info=YKA7cU(function(IPPy,VspvGB9V,LrFLp5)if LrFLp5 ==nil then LrFLp5="*"end;if pX4gCR(VspvGB9V)then
    iPL3B4cr.fatal("Usage: hpm hel:info <package name> [<version specification>]")end
    if pX4gCR(LrFLp5)then LrFLp5="*"end
    iPL3B4cr.print("Creating version specification for "..tostring(LrFLp5).." ...")
    local GfB7,Iz_w1j=pcall(function()return u.Spec(LrFLp5)end)if not(GfB7)then
      iPL3B4cr.fatal("Could not parse the version specification: "..tostring(Iz_w1j).."!")end
    local G=IPPy:getPackageSpec(VspvGB9V)local X7YKzX=IPPy:parsePackageJSON(G,Iz_w1j)local od0VOF={}
    CM(od0VOF,
        "- Package name:   "..tostring(G.name))
    CM(od0VOF,"- Description:\n"..tostring(G.description))
    CM(od0VOF,"- Package owners: "..tostring(table.concat(G.owners,", ")))
    CM(od0VOF,"- Authors:\n"..
        tostring(table.concat((function()local oO6SbZ={}local UE_vrsNx=1;local kef2zBS=G.authors;for Z=1,#kef2zBS do
          local ze0=kef2zBS[Z]oO6SbZ[UE_vrsNx]="  - "..tostring(ze0)
          UE_vrsNx=UE_vrsNx+1 end;return oO6SbZ end)(),"\n")))
    CM(od0VOF,"- License:        "..tostring(G.license))
    CM(od0VOF,"- Versions:       "..tostring(VcV0EuD(G.versions))..", latest: "..
        tostring(X7YKzX.version))
    CM(od0VOF,"  - Files:        "..tostring(#X7YKzX.files))
    CM(od0VOF,"  - Depends:      "..
        tostring(table.concat((function()local ylW3uC0={}local N_G1=1;local wkGNE=X7YKzX.dependencies
          for ccK=1,#wkGNE
          do local BV=wkGNE[ccK]ylW3uC0[N_G1]=tostring(BV.name)..
              "@"..tostring(BV.version)N_G1=N_G1+1 end;return ylW3uC0 end)(),"  ")))
    CM(od0VOF,"  - Changes:\n"..
        tostring(G.versions[X7YKzX.version].changes))CM(od0VOF,"- Stats:")
    CM(od0VOF,"  - Views:        "..tostring(G.stats.views))
    CM(od0VOF,"- Creation date:  "..tostring(G.stats.date.created).." UTC")
    CM(od0VOF,"- Last updated:   "..
        tostring(G.stats.date["last-updated"]).." UTC")
    return iPL3B4cr.print(table.concat(od0VOF,"\n"))end)
  IPPy.search=YKA7cU(function(IPPy,...)local HnLY=0
    while true do local cm51CH1n={}local iWrSgT=IPPy.URL.."packages"iWrSgT=iWrSgT..
        "?offset="..tostring(HnLY)
      if...then
        iWrSgT=iWrSgT..
            ("&q="..
                table.concat((function(...)
                  local Ub9iqg={}local r_S8HFRo=1;local qIF4RFBv={...}
                  for wNbC65Ta=1,#qIF4RFBv do local xOiPW=qIF4RFBv[wNbC65Ta]Ub9iqg[r_S8HFRo]=
                  '"'..xOiPW:gsub("\"","")..'"'r_S8HFRo=
                  r_S8HFRo+1 end;return Ub9iqg end)(...)," "):gsub("&",""))end;local C,YK1=wjim8xCV(iWrSgT)if not(C)then
      iPL3B4cr.fatal("HTTP request error: "..YK1)end;local t96Qtz=""
      for Z9j in YK1 do t96Qtz=t96Qtz..Z9j end;local HjKNi=JD:decode(t96Qtz)if not(HjKNi)then
      iPL3B4cr.fatal("Incorrect JSON format!\n"..
          tostring(t96Qtz))end;cm51CH1n=HjKNi.data.list;for r=1,#
    cm51CH1n do local OnJ1=cm51CH1n[r]
      iPL3B4cr.print(tostring(OnJ1.name)..": "..
          tostring(OnJ1.short_description))end;if
    #cm51CH1n==0 and HnLY==0 then
      iPL3B4cr.print("No packages found.")break end
      if
      HjKNi.data.truncated and HjKNi.data.sent+
          HjKNi.data.offset<HjKNi.data.total then HnLY=HjKNi.data.offset+HjKNi.data.sent else break end end end)
  if rQYWEt.__inherited then rQYWEt.__inherited(rQYWEt,PKWIJ9)end;xSebv5Jc.hel=PKWIJ9 end
do local KFU0;local Pvuq=xSebv5Jc.default;local lOpDJ={}lOpDJ.__index=lOpDJ
  setmetatable(lOpDJ,Pvuq.__base)
  KFU0=setmetatable({__init=function(lTH,...)return KFU0.__parent.__init(lTH,...)end,__base=lOpDJ,__name="oppm",__parent=Pvuq},{__index=function(JL,FpU_E)
    local JWtwnQ2t=rawget(lOpDJ,FpU_E)
    if JWtwnQ2t==nil then local uEKPPpj_=rawget(JL,"__parent")if uEKPPpj_ then
      return uEKPPpj_[FpU_E]end else return JWtwnQ2t end end,__call=function(aYO4NN,...)
    local CtG9nSQL=setmetatable({},lOpDJ)aYO4NN.__init(CtG9nSQL,...)return CtG9nSQL end})lOpDJ.__class=KFU0;local YLe=KFU0
  YLe.REPOS="https://raw.githubusercontent.com/OpenPrograms/openprograms.github.io/master/repos.cfg"YLe.PACKAGES="https://raw.githubusercontent.com/%s/master/programs.cfg"
  YLe.FILES="https://raw.githubusercontent.com/%s/%s"YLe.DIRECTORY="https://api.github.com/repos/%s/contents/%s?ref=%s"
  YLe.DEFAULT_CACHE_FILE="/var/cache/hpm/oppm"
  YLe.cacheFile=function(YLe)
    local uZtK5yX=mMp.get("oppm",{},true).get("cache_file",YLe.DEFAULT_CACHE_FILE)
    if not(dk(pfZ3SPy_.path(uZtK5yX)))then
      local kr2CYaS,hXgSzEI=pE(pfZ3SPy_.path(uZtK5yX))if not(kr2CYaS)then
      iPL3B4cr.fatal("Could not create the cache directory at "..
          tostring(pfZ3SPy_.path(uZtK5yX))..": "..tostring(hXgSzEI))end end
    if not(E(uZtK5yX))then local AUQ,B=io.open(uZtK5yX,"w")if not(AUQ)then
      iPL3B4cr.fatal(
          "Could not open '"..tostring(uZtK5yX).."' for writing: "..tostring(B))end
      AUQ:write("{}")AUQ:close()end;return uZtK5yX end
  YLe.notifyCacheUpdate=function(YLe,J)local coSiE=wZdg()local wm=coSiE-J
    if wm>24*60*60*1000 then
      iPL3B4cr.print("Cache was last updated more than a day ago.")
      return iPL3B4cr.print("Consider running hpm oppm:cache update to update it.")end end
  YLe.listCache=function(YLe)local _O;local smj=YLe:cacheFile()
    do local obBu,cbQlG=io.open(smj,"r")if not
    (obBu)then
      return false,"Could not open '"..
          tostring(smj).."' for reading: "..tostring(cbQlG)end
      gad4ZcL=obBu:read("*all")_O,cbQlG=Do6yo7nm(gad4ZcL)if not(_O)then return false,
    "Cache is malformed: "..tostring(cbQlG)end
      obBu:close()end
    if not _O.updated or not _O.cache then _O={updated=0,cache=_O}end;return _O end
  YLe.resolveDirectory=function(YLe,YZQu1DR4,kza,CvGDk_2)
    local EGpun=n(yY(YLe.DIRECTORY:format(YZQu1DR4,CvGDk_2,kza)))EGpun=JD:decode(EGpun)
    if EGpun.message then return false,
    "Could not fetch "..tostring(YZQu1DR4)..
        ":"..
        tostring(kza).."/"..
        tostring(CvGDk_2)..": "..tostring(EGpun.message)end;local LNlhK={}local cnx_1g=1
    for eV=1,#EGpun do local DGQnw=EGpun[eV]
      if DGQnw.type=="file"then
        LNlhK[cnx_1g]={name=DGQnw.name,url=DGQnw.download_url,path=DGQnw.path}cnx_1g=cnx_1g+1 end end;return LNlhK end
  YLe.updateCache=function(YLe)local yLgHuF=YLe:cacheFile()local fpL,k6=YLe:listCache()if
  not(fpL)then
    iPL3B4cr.error("Old cache is malformed: "..tostring(fpL))fpL={}else fpL=fpL.cache end;local m
    m,k6=yY(YLe.REPOS)
    if not(m)then return false,"Could not fetch "..
        tostring(YLe.REPOS)..": "..tostring(k6)end;m=Do6yo7nm(m)local rvNhq6v={}
    for WV,yUho4MXRx in
    pairs(iW6CD(mMp.get("oppm",{},true).get("custom_repos",{})))do rvNhq6v["local/"..tostring(WV)]=yUho4MXRx end;M6ilzGJ(m,rvNhq6v)local gC={}
    for J2,hgrBfz0w in pairs(m)do local Gi=false
      repeat
        if hgrBfz0w.repo then
          iPL3B4cr.info(
              "Fetching '"..tostring(J2)..
                  "' at '"..tostring(hgrBfz0w.repo).."' ...")local wpv1,I9IMuWm
          wpv1,I9IMuWm,k6=wjim8xCV(YLe.PACKAGES:format(hgrBfz0w.repo))
          if not(wpv1 and I9IMuWm)then
            iPL3B4cr.error("Could not fetch '"..
                tostring(J2).."' at '"..
                tostring(hgrBfz0w.repo).."': "..tostring(k6))Gi=true;break end;local a=""
          for wpv1,VKTNfzUf in function()return pcall(I9IMuWm)end do
            if not wpv1 then
              iPL3B4cr.error("Could not fetch '"..
                  tostring(J2).."' at '"..
                  tostring(hgrBfz0w.repo).."': "..tostring(VKTNfzUf))a=false;break else if not VKTNfzUf then break end;a=a..VKTNfzUf end end;if a==false then Gi=true;break end;if pX4gCR(a)then
          iPL3B4cr.error("Could not fetch '"..
              tostring(J2)..
              "' at '"..tostring(hgrBfz0w.repo).."'")Gi=true;break end
          local rZ;rZ,k6=Do6yo7nm(a)
          if not rZ then
            iPL3B4cr.error("Manifest '"..tostring(J2)..
                "' at '"..
                tostring(hgrBfz0w.repo).."' is malformed: "..tostring(k6))Gi=true;break end
          for Oms4,JfA in pairs(rZ)do local CPu1=false
            repeat
              if Oms4:match("[^A-Za-z0-9._-]")then
                iPL3B4cr.error(
                    "Package name contains illegal characters: "..tostring(J2)..":"..tostring(Oms4).."!")CPu1=true;break end
              CM(gC,{repo=hgrBfz0w.repo,name=Oms4,data=JfA})CPu1=true until true;if not CPu1 then break end end end;Gi=true until true;if not Gi then break end end;local QO={}local VvzMQHj={}
    for pfyhF=1,#gC do local pglFz82w=gC[pfyhF]local RkeCL,LoW_7e,mLgQ
      RkeCL,LoW_7e,mLgQ=pglFz82w.name,pglFz82w.repo,pglFz82w.data;if tE(RSjapQ(LoW_7e,RkeCL),QO)then
      iPL3B4cr.error("There're multiple packages under the same name: "..
          tostring(RkeCL).."!")end
      CM(VvzMQHj,{pkg=RkeCL,repo=LoW_7e,data={name=RkeCL,repo=LoW_7e,data=mLgQ}})local ng;do
      for Pp_NboV,owAp3u2G in pairs(fpL)do if
      owAp3u2G.repo==LoW_7e and owAp3u2G.pkg==RkeCL then ng=Pp_NboV;break end end end
      if ng then
        table.remove(fpL,ng)else CM(QO,RSjapQ(LoW_7e,RkeCL))end end;local fSYJX;fSYJX,k6=io.open(yLgHuF,"w")
    if not(fSYJX)then return false,
    "Could not open '"..
        tostring(yLgHuF).."' for writing: "..tostring(k6)end;do
      fSYJX:write(pDNa2ox6({updated=wZdg(),cache=VvzMQHj}))fSYJX:close()end
    iPL3B4cr.print("- "..

        tostring(#gC).." program"..tostring(OO(#gC)).." cached.")
    iPL3B4cr.print("- "..tostring(#QO)..
        " package"..tostring(OO(#QO)).." "..
        tostring(cR6rJlAl(#QO)).." new.")
    iPL3B4cr.print("- "..tostring(#fpL)..
        " package"..tostring(OO(#fpL)).." no longer exist"..
        tostring(y(#fpL))..".")return true end
  YLe.parseLocalPath=function(YLe,OH0C,kmQkm9cr)
    if kmQkm9cr:sub(1,2)=="//"then return
    RSjapQ(OH0C,kmQkm9cr:sub(3))else return RSjapQ(OH0C,"usr",kmQkm9cr)end end
  YLe.rawInstall=function(YLe,IE97m,wey,hThO6,zXU)if wey==nil then wey="/"end;if hThO6 ==nil then hThO6=false end;if zXU==
      nil then zXU=false end;local HmJym2=n(YLe:listCache())
    HmJym2=HmJym2.cache;local Jjb7Am5={filesInstalled=0,packagesInstalled=0}
    if zXU and not dk(wey)then
      local gA,r=pE(wey)if not(gA)then
      iPL3B4cr.fatal("Failed to create '"..
          tostring(wey).."' directory for package '"..tostring(IE97m).."'! \n"..
          tostring(r))end elseif not zXU then local LWe=h6Ub7U(IE97m,
        nil,"oppm")if LWe then
      iPL3B4cr.print("'"..tostring(IE97m)..
          "' is already installed, skipping...")return LWe,Jjb7Am5 end end;local UwqY7A
    for _3Tq=1,#HmJym2 do local Rq1hByv=HmJym2[_3Tq]local iFk,sEFtmNgB,qxiez0Cn
      iFk,sEFtmNgB,qxiez0Cn=Rq1hByv.pkg,Rq1hByv.repo,Rq1hByv.data;if iFk==IE97m then UwqY7A=Rq1hByv;break end end;if not(UwqY7A)then
      iPL3B4cr.fatal("No such package: "..tostring(IE97m))end;local k={}local d7gPKcw=UwqY7A.repo
    for Ck_H,Sc in
    pairs(UwqY7A.data.data.files)do local _QFw_It={}
      if Ck_H:sub(1,1)==":"then
        _QFw_It=YLe:resolveDirectory(d7gPKcw,Ck_H:sub(2,Ck_H:find("/")-1,
            nil),Ck_H:sub(Ck_H:find("/")+1))elseif Ck_H:sub(1,1)=="?"then
        if
        wVzn(RSjapQ(YLe:parseLocalPath(wey,Sc),pfZ3SPy_.name(Ck_H)))then _QFw_It={}else local WLqHf=Ck_H:sub(2,-1)
          _QFw_It={{name=pfZ3SPy_.name(WLqHf),path=WLqHf,url=YLe.FILES:format(d7gPKcw,WLqHf)}}end else
        _QFw_It={{name=pfZ3SPy_.name(Ck_H),path=Ck_H,url=YLe.FILES:format(d7gPKcw,Ck_H)}}end;local IE97m
      for vN=1,#_QFw_It do local BIwW6_=_QFw_It[vN]local Vdfc3,CzM7PG
        IE97m,Vdfc3,CzM7PG=BIwW6_.name,BIwW6_.path,BIwW6_.url;local RKf6s5=n(yY(CzM7PG))local tP9E_=YLe:parseLocalPath(wey,Sc)if not
      (dk(tP9E_))then pE(tP9E_)end
        do
          local Y1WX,G06Z2=io.open(RSjapQ(tP9E_,IE97m),"w")if not Y1WX then
          iPL3B4cr.fatal("Could not open file for writing: "..tostring(G06Z2))end;Y1WX:write(RKf6s5)
          Y1WX:close()end;Jjb7Am5.filesInstalled=Jjb7Am5.filesInstalled+1
        CM(k,{name=IE97m,url=CzM7PG,dir=tP9E_})end end;local naeNp={}
    if UwqY7A.data.data.dependencies then for K in
    pairs(UwqY7A.data.data.dependencies)do CM(naeNp,{name=K})end end;Jjb7Am5.packagesInstalled=Jjb7Am5.packagesInstalled+1;return
    {name=IE97m,files=k,dependencies=naeNp,manual=hThO6},Jjb7Am5 end
  YLe.resolveDependencies=function(YLe,tQx9TV,FL7g2o,dkh7Tt9,XiNd_H,Q_c4px86)if dkh7Tt9 ==nil then dkh7Tt9={}end
    if XiNd_H==nil then XiNd_H={}end;if Q_c4px86 ==nil then Q_c4px86={}end
    local _F6VYt=n(YLe:listCache())_F6VYt=_F6VYt.cache
    for ITv3PH1i=1,#tQx9TV do local _5fF=tQx9TV[ITv3PH1i]local OUQqQp=false
      for OyOfzTWn=1,#dkh7Tt9
      do local rx=dkh7Tt9[OyOfzTWn]if rx==_5fF then OUQqQp=true;break end end
      if not(OUQqQp)then XiNd_H[_5fF]=true;local ijvSrZA1=h6Ub7U(_5fF,nil,"oppm")
        if not
        ijvSrZA1 then local STNuSN6
          for PYOeGnAZ=1,#_F6VYt do local s10ar5XH=_F6VYt[PYOeGnAZ]local YoKhvIs
            YoKhvIs=s10ar5XH.pkg;if YoKhvIs==_5fF then STNuSN6=s10ar5XH;break end end;if not(STNuSN6)then
          return false,"Unknown package: "..tostring(_5fF)end
          if STNuSN6.data.data.dependencies then
            for I2ipE in
            pairs(STNuSN6.data.data.dependencies)do OUQqQp=false;for qS730I=1,#dkh7Tt9 do local PYEbnua=dkh7Tt9[qS730I]
              if PYEbnua==I2ipE then OUQqQp=true;break end end
              if not(OUQqQp)then
                if
                XiNd_H[I2ipE]then
                  iPL3B4cr.fatal("Circular dependencies detected: '"..
                      tostring(_5fF).."' depends on '"..
                      tostring(I2ipE).."', and '"..

                      tostring(I2ipE).."' depends on '"..tostring(_5fF).."'.")end
                YLe:resolveDependencies({I2ipE},false,dkh7Tt9,XiNd_H,Q_c4px86)end end end;CM(Q_c4px86,_5fF)else if FL7g2o then CM(Q_c4px86,_5fF)end end;CM(dkh7Tt9,_5fF)XiNd_H[_5fF]=nil end end;return Q_c4px86 end
  YLe.getPackageDependants=function(YLe,Um4ZYiT,AF,s)if AF==nil then AF={}end;if s==nil then s={}end
    for hIHW=1,#Um4ZYiT do
      local H5=Um4ZYiT[hIHW]local HYY=false;for C3=1,#AF do local SkCMMH=AF[C3]
      if SkCMMH.name==H5 then HYY=true;break end end
      if not(HYY)then CM(s,{name=H5})
        local kvvs=h6Ub7U(H5,nil,"oppm")
        if kvvs then CM(AF,{name=H5,manifest=kvvs})
          local _yTx3S94=n(_(RSjapQ(z,"oppm")))
          for Mm in _yTx3S94 do kvvs=n(h6Ub7U(Mm,nil,"oppm"))
            local g524=kvvs.dependencies
            for WUdVeYc=1,#g524 do local lHep6wo=g524[WUdVeYc]
              if lHep6wo.name==H5 then HYY=false
                for BKZsJ=1,#AF do
                  local Sw=AF[BKZsJ]if Sw.name==Mm then HYY=true;break end end
                if not HYY then
                  for W67mm9p6=1,#s do local oBxdTi6u=s[W67mm9p6]if oBxdTi6u.name==Mm then
                    iPL3B4cr.fatal(
                        "Circular dependencies detected: "..tostring(Mm))end end;YLe:getPackageDependants({Mm},AF,s)end end end end else
          iPL3B4cr.fatal("Package "..
              tostring(H5).." is referenced as a dependant of another package, however, this package isn't installed.")end;s[#s]=nil end end;return AF end
  YLe.whatDependsOn=function(YLe,T7hLe5j)local I_=n(h6Ub7U(T7hLe5j,nil,"oppm"))
    local J2Jin={}local Rvg=n(_(RSjapQ(z,"oppm")))
    for HpdA in Rvg do
      I_=n(h6Ub7U(HpdA,nil,"oppm"))local DsAJbW=I_.dependencies
      for AXfX=1,#DsAJbW do local btcUUhB=DsAJbW[AXfX]if
      btcUUhB.name==T7hLe5j then CM(J2Jin,HpdA)end end end;return J2Jin end
  YLe.install=YKA7cU(function(YLe,...)local iw0S=n(YLe:listCache())
    YLe:notifyCacheUpdate(iw0S.updated)local Tjg={...}local n2srE7H=RkGFh6.r or RkGFh6.reinstall;local Rf=RkGFh6.s or
        RkGFh6.save
    local X9ZjrTz=n(YLe:resolveDependencies(Tjg,n2srE7H))
    UlFdiZ7v({install=(function()local Ht5Ge={}local l=1
      for IO=1,#X9ZjrTz do local YDJY=X9ZjrTz[IO]if
      not n2srE7H or not tE(YDJY,Tjg)then Ht5Ge[l]=YDJY;l=l+1 end end;return Ht5Ge end)(),reinstall=

              n2srE7H and
                  (function()local t={}local Rdi8NIft=1
                    for J0uTkQ9=1,#X9ZjrTz do local sd6k=X9ZjrTz[J0uTkQ9]if tE(sd6k,Tjg)then
                      t[Rdi8NIft]=sd6k;Rdi8NIft=Rdi8NIft+1 end end;return t end)()or nil})local tYFIuD={filesInstalled=0,packagesInstalled=0}
    if n2srE7H then local a
      do local lK7={}local KWMxs7a=1;for T=1,#Tjg do
        local LBIp4=Tjg[T]lK7[KWMxs7a]=n(h6Ub7U(LBIp4,nil,"oppm"))
        KWMxs7a=KWMxs7a+1 end;a=lK7 end;YLe:_remove(a,true,false)end
    for A5=1,#X9ZjrTz do local PV168s0f=X9ZjrTz[A5]
      iPL3B4cr.print("Installing '"..
          tostring(PV168s0f).."'...")local bjK;if Rf then bjK="./"..tostring(PV168s0f).."/"else
      bjK="/"end
      local Us1Xh,rs59=YLe:rawInstall(PV168s0f,bjK,tE(PV168s0f,Tjg),Rf)
      tYFIuD.filesInstalled=tYFIuD.filesInstalled+rs59.filesInstalled
      tYFIuD.packagesInstalled=tYFIuD.packagesInstalled+rs59.packagesInstalled
      if tYFIuD.packagesInstalled~=0 then local R,rGa2MaGH=unArcvQl(Us1Xh,"oppm")
        if R then
          iPL3B4cr.info(
              "Saved the manifest of '"..tostring(Us1Xh.name).."'.")else
          iPL3B4cr.fatal("Couldn't save the manifest of '"..tostring(Us1Xh.name).."': "..
              tostring(rGa2MaGH)..".")end end end
    iPL3B4cr.print("- "..
        tostring(tYFIuD.packagesInstalled).." package"..
        tostring(OO(tYFIuD.packagesInstalled)).." installed.")
    iPL3B4cr.print("- "..
        tostring(tYFIuD.filesInstalled).." file"..
        tostring(OO(tYFIuD.filesInstalled)).." installed.")return iPL3B4cr.print("Done.")end)
  YLe.remove=YKA7cU(function(YLe,...)local i6={...}local u33wPQT={}for aNrMnPZ=1,#i6 do local fC=i6[aNrMnPZ]
    local Kl=n(h6Ub7U(fC,nil,"oppm"))CM(u33wPQT,Kl)end
    YLe:_remove(u33wPQT,false)return iPL3B4cr.print("Done.")end)
  YLe._remove=function(YLe,EmJGBwA,_E3,j3)if _E3 ==nil then _E3=false end;if j3 ==nil then j3=true end;local f
    if not
    mMp.get("oppm",{},true).get("remove_dependants",true)or not j3 then
      do local jy={}
        local Ifev2bUE=1
        for ZY=1,#EmJGBwA do local KCpJbzHT=EmJGBwA[ZY]
          jy[Ifev2bUE]={name=KCpJbzHT.name,manifest=KCpJbzHT}Ifev2bUE=Ifev2bUE+1 end;f=jy end else
      f=YLe:getPackageDependants((function()local g={}local dQl0xvy2=1;for hX=1,#EmJGBwA do local wYTrvPn=EmJGBwA[hX]
        g[dQl0xvy2]=wYTrvPn.name;dQl0xvy2=dQl0xvy2+1 end;return g end)())end
    if not(_E3)then
      UlFdiZ7v({remove=(function()local pB6K={}local YV=1;for zPm=1,#f do local JmEyZ5=f[zPm]
        pB6K[YV]=tostring(JmEyZ5.name)YV=YV+1 end;return pB6K end)()})end
    for FGvy=1,#f do local KpnA=f[FGvy]
      iPL3B4cr.print("Removing '"..
          tostring(KpnA.manifest.name).."' ...")
      n(KFU0.__parent.remove(YLe,KpnA.manifest,"oppm"))end;return true end
  YLe.cache=YKA7cU(function(YLe,j_F9c,...)local q=j_F9c
    if"update"==q then
      iPL3B4cr.print("Updating OpenPrograms program cache ...")n(YLe:updateCache())
      return iPL3B4cr.print("Done.")else iPL3B4cr.error("Unknown command.")return
    iPL3B4cr.print("Usage: hpm oppm:cache update")end end)
  YLe.autoremove=YKA7cU(function(YLe)local b7G0ciz={}local rF2te={}
    local KG_EjN=n(_(RSjapQ(z,"oppm")))
    for aIrjXeB in KG_EjN do local sZdri=n(h6Ub7U(aIrjXeB,nil,"oppm"))
      if not
      (sZdri.manual)then local pT=YLe:getPackageDependants(aIrjXeB)if#pT==1 then
        CM(b7G0ciz,aIrjXeB)CM(rF2te,aIrjXeB)end end end
    while true do local XgkgIR9=false;KG_EjN=n(_(RSjapQ(z,"oppm")))
      for sm2 in KG_EjN do
        if not
        (tE(sm2,b7G0ciz))then local cz=n(h6Ub7U(sm2,nil,"oppm"))
          if
          not(cz.manual)then local pSL=YLe:getPackageDependants(sm2)
            table.remove(pSL,1)
            if
            gad4ZcL((function()local ifrP9={}local Iynmp=1;for PFvHX=1,#pSL do local sP=pSL[PFvHX]
              ifrP9[Iynmp]=tE(sP.name,b7G0ciz)Iynmp=Iynmp+1 end;return ifrP9 end)())then
              for Y=1,#pSL do local QHxdp58D=pSL[Y]local efdknL,YUdva=tE(QHxdp58D.name,rF2te)if YUdva then
                table.remove(rF2te,YUdva)end end;CM(b7G0ciz,sm2)CM(rF2te,sm2)XgkgIR9=true end end end end;if not(XgkgIR9)then break end end
    UlFdiZ7v({remove=(function()
      if#b7G0ciz>0 then local x8FBS={}local LGBr=1
        for M=1,#b7G0ciz do local I=b7G0ciz[M]x8FBS[LGBr]="oppm:"..
            tostring(I)LGBr=LGBr+1 end;return x8FBS else return nil end end)()})for W=1,#rF2te do local Dx5GC=rF2te[W]
      YLe:_remove({n(h6Ub7U(Dx5GC,nil,"oppm"))},false)end
    iPL3B4cr.print("Done.")return true end)
  YLe.search=YKA7cU(function(YLe,...)local kwZhI=n(YLe:listCache())
    YLe:notifyCacheUpdate(kwZhI.updated)local T0h=n(kwZhI.cache)local H0={}
    if...then
      for Mrz66=1,#T0h do local A=T0h[Mrz66]local RR;RR=A.data
        local Oj4B=RR.data;local aT={...}
        for ZN0brC=#aT,1,-1 do local g=aT[ZN0brC]if RR.name:find(g)then
          table.remove(aT,ZN0brC)break end;if Oj4B.name and Oj4B.name:find(g)then
          table.remove(aT,ZN0brC)break end
          if Oj4B.description and
              Oj4B.description:find(g)then table.remove(aT,ZN0brC)break end;if Oj4B.note and Oj4B.note:find(g)then
          table.remove(aT,ZN0brC)break end end;if#aT==0 then CM(H0,RR)end end else
      do local iYIip_rt={}local WoJla=1;for jk=1,#T0h do local Y=T0h[jk]iYIip_rt[WoJla]=Y.data
        WoJla=WoJla+1 end;H0=iYIip_rt end end
    for sM=1,#H0 do local MMJEx=H0[sM]
      iPL3B4cr.print(tostring(MMJEx.name)..
          " - "..tostring(MMJEx.data.name or
          MMJEx.name)..
          ": "..tostring(MMJEx.data.description))end end)
  YLe.info=YKA7cU(function(YLe,EB)if pX4gCR(EB)then
    iPL3B4cr.fatal("Usage: hpm oppm:info <package name>")end
    local Qy=n(YLe:listCache())Qy=Qy.cache;local rCWKBim=nil;for i=1,#Qy do local wLI=Qy[i]
      if wLI.pkg==EB then rCWKBim=wLI;break end end;if not(rCWKBim)then
      iPL3B4cr.fatal("No such package.")end
    iPL3B4cr.print("- Package name: "..tostring(rCWKBim.pkg))if rCWKBim.data.data.name then
      iPL3B4cr.print("                "..
          tostring(rCWKBim.data.data.name))end;if
    rCWKBim.data.data.description then
      iPL3B4cr.print("- Description:\n"..
          tostring(rCWKBim.data.data.description))end;if
    rCWKBim.data.data.authors then
      iPL3B4cr.print("- Authors:\n"..tostring(rCWKBim.data.data.authors))end;if
    rCWKBim.data.data.files then
      iPL3B4cr.print("- Files:        "..
          tostring(VcV0EuD(rCWKBim.data.data.files)))end
    if
    rCWKBim.data.data.dependencies then
      iPL3B4cr.print("- Depends:      "..
          tostring(table.concat((function()local N7h={}local jDOa=1
            for DcVHvlcZ in
            pairs(rCWKBim.data.data.dependencies)do N7h[jDOa]=DcVHvlcZ;jDOa=jDOa+1 end;return N7h end)(),"  ")))end;if rCWKBim.data.data.note then
      iPL3B4cr.print("- Note:\n"..
          tostring(rCWKBim.data.data.note))end;return
    iPL3B4cr.print(
        "- Repository:   https://github.com/"..tostring(rCWKBim.repo))end)
  if Pvuq.__inherited then Pvuq.__inherited(Pvuq,KFU0)end;xSebv5Jc.oppm=KFU0 end;local U
U=function()local Z=n(_(z))pX4gCR=true
  for Ke2eY9 in Z do local mJU4=pfZ3SPy_.name(Ke2eY9)
    if
    JQi1jg(RSjapQ(z,mJU4))then local ywtz=n(_(RSjapQ(z,mJU4)))
      for N7 in ywtz do
        if not
        (JQi1jg(RSjapQ(z,mJU4,N7)))then local LLka3VWH=n(h6Ub7U(N7,nil,mJU4))
          iPL3B4cr.print(
              mJU4 ..":"..N7 ..
                  (LLka3VWH.version and" @ "..LLka3VWH.version or""))pX4gCR=false end end end end
  if pX4gCR then return iPL3B4cr.print("No packages installed.")end end;local wFeA
wFeA=function(...)hw18,RkGFh6=XPoQB(...)if#hw18 <1 then return PG()end end;local JQgI
JQgI=function()local B34=hw18[1]
  if"list"==B34 then return U()elseif"help"==B34 then return PG()else
    do
      local S9D=qTDt(hw18[1])
      if S9D then
        return
        S9D(Qlmlet((function()local JeqL={}local RmN8=1;for ePtDYbn=2,#hw18 do local dPm5lS=hw18[ePtDYbn]JeqL[RmN8]=dPm5lS
          RmN8=RmN8+1 end;return JeqL end)()))end end end end
rDtVf.semver={Version=u.Version,Spec=u.Spec,SpecItem=u.SpecItem,compare=u.compare,match=u.match,validate=u.validate}rDtVf.json=JD;rDtVf.CONFIG_PATH=ykRppH;rDtVf.USAGE=WQ6
rDtVf.DEFAULT_CONFIG=y36Aetn;rDtVf.options=RkGFh6;rDtVf.args=hw18;rDtVf.request=nvCiFt7r
rDtVf.modules=xSebv5Jc;rDtVf.config=mMp;rDtVf.modulePath=vj;rDtVf.distPath=z
rDtVf.exitCode=Zg;rDtVf.log=iPL3B4cr;rDtVf.assert=GI2hz6SK;rDtVf.unimplemented=Oh
rDtVf.printUsage=PG;rDtVf.try=n;rDtVf.checkType=O;rDtVf.argNumber=N5UjTN
rDtVf.argString=qLH5;rDtVf.isin=tE;rDtVf.tableLen=VcV0EuD;rDtVf.empty=pX4gCR
rDtVf.all=gad4ZcL;rDtVf.existsDir=dk;rDtVf.existsFile=E;rDtVf.plural=OO;rDtVf.singular=y
rDtVf.linkingVerb=cR6rJlAl;rDtVf.remove=BaX;rDtVf.loadConfig=SJsW11k;rDtVf.checkInternet=Ki1HJT
rDtVf.download=wjim8xCV;rDtVf.findCustomCommand=qTDt;rDtVf.getModuleBy=v
rDtVf.callModuleMethod=Ta;rDtVf.saveManifest=unArcvQl;rDtVf.loadManifest=h6Ub7U
rDtVf.removeManifest=Gm;rDtVf.public=YKA7cU;rDtVf.wrapResponse=mCsewfX;rDtVf.recv=yY
rDtVf.confirm=Xf;rDtVf.pkgPlan=UlFdiZ7v;rDtVf.printPackageList=U
rDtVf.parseArguments=wFeA;rDtVf.process=JQgI;for fNIe,y2R1 in pairs(_G)do rDtVf[fNIe]=y2R1 end
wFeA(...)n(SJsW11k())EQLam()JQgI()return Zg
